package com.ajpr00.mobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.model.EstadoDispositivo
import com.ajpr00.core.domain.model.PendingMedia
import com.ajpr00.core.domain.model.PendingStatus
import com.ajpr00.core.domain.model.RemoteMedia
import com.ajpr00.core.domain.model.mDNS.MdnsServiceInfo
import com.ajpr00.core.domain.usecase.media.AddPendingMediaUseCase
import com.ajpr00.core.domain.usecase.dispositivo.DeleteDispositivoUseCase
import com.ajpr00.core.domain.usecase.dispositivo.DiscovermDNSTabletUseCase
import com.ajpr00.core.domain.usecase.media.GetAllPendingMediaUseCase
import com.ajpr00.core.domain.usecase.dispositivo.GetDispositivosUseCase
import com.ajpr00.core.domain.usecase.media.GetNextPendingMediaUseCase
import com.ajpr00.core.domain.usecase.dispositivo.UpdateDispositivoUseCase
import com.ajpr00.core.domain.usecase.media.GetFlowRemoteMediaWithThumbnailsUseCase
import com.ajpr00.core.domain.usecase.media.SendEncryptedMediaUseCase
import com.ajpr00.core.domain.usecase.media.UpdatePendingMediaStatusUseCase
import com.ajpr00.core.util.tryLocate
import com.ajpr00.mobile.ui.mapper.toDispositivoUi
import com.ajpr00.mobile.ui.model.DispositivoUi
import com.ajpr00.visumloop.mobile.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * # PanelControlViewModel
 *
 * ViewModel principal de la pantalla de control del móvil.
 *
 * ## ¿Qué hace este ViewModel?
 * - Gestiona el **dispositivo seleccionado** (tablet).
 * - Observa la **lista de media remota** en tiempo real.
 * - Gestiona la **cola de archivos pendientes** de enviar.
 * - Controla el **estado ONLINE/OFFLINE** de cada tablet.
 * - Inicia el **descubrimiento mDNS**.
 * - Lanza el **envío cifrado** de archivos al servidor NanoHTTPD.
 *
 * ## Rol dentro de la arquitectura
 * - **Presentation layer**: expone `StateFlow` para Compose.
 * - **No contiene lógica de infraestructura** (HTTP, cifrado, Room).
 * - **No contiene lógica de parseo** (eso está en mappers).
 * - **Solo orquesta UseCases** de la capa domain.
 *
 * ## Notas importantes
 * - Todos los logs están pensados para depuración pedagógica.
 * - No se bloquea el hilo principal; todo va por coroutines.
 * - El ViewModel nunca toca Retrofit ni Room directamente.
 */
@HiltViewModel
class PanelControlViewModel @Inject constructor(
    private val getDispositivosUseCase: GetDispositivosUseCase,
    private val deleteDispositivoUseCase: DeleteDispositivoUseCase,
    private val updateDispositivoUseCase: UpdateDispositivoUseCase,
    private val addPendingMediaUseCase: AddPendingMediaUseCase,
    private val getNextPendingMediaUseCase: GetNextPendingMediaUseCase,
    private val updatePendingMediaStatusUseCase: UpdatePendingMediaStatusUseCase,
    private val getAllPendingMediaUseCase: GetAllPendingMediaUseCase,
    private val getObserveListUseCase: GetFlowRemoteMediaWithThumbnailsUseCase,
    private val discovermDNSTabletUseCase: DiscovermDNSTabletUseCase,
    private val sendEncryptedMediaUseCase: SendEncryptedMediaUseCase,
) : ViewModel() {

    private val TAG = "PanelControlVM"

    // ---------------------------------------------------------
    //  DISPOSITIVO SELECCIONADO
    // ---------------------------------------------------------

    /**
     * Estado interno del dispositivo seleccionado.
     *
     * - `null` → no hay tablet seleccionada.
     * - `DispositivoUi` → tablet activa.
     *
     * La UI observa `selectedDevice`.
     */
    private val _selectedDevice = MutableStateFlow<DispositivoUi?>(null)

    /**
     * Estado público del dispositivo seleccionado.
     */
    val selectedDevice: StateFlow<DispositivoUi?> = _selectedDevice

    /**
     * Último servicio mDNS detectado.
     *
     * La UI lo usa para mostrar tablets disponibles en la red.
     */
    private val _mdnsState = MutableStateFlow<MdnsServiceInfo?>(null)
    val mdnsState: StateFlow<MdnsServiceInfo?> = _mdnsState

    // ---------------------------------------------------------
    //  LISTA REMOTA DE MEDIA (STREAMING)
    // ---------------------------------------------------------

    /**
     * Lista de media remota obtenida desde la tablet seleccionada.
     *
     * Flujo:
     * 1. Espera a que haya un dispositivo seleccionado.
     * 2. Llama al UseCase que observa media + thumbnails en tiempo real.
     * 3. Expone un StateFlow para Compose.
     *
     * Advertencia:
     * - Si `selectedDevice` es null, la lista es vacía.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val listRepro: StateFlow<List<RemoteMedia>> =
        selectedDevice
            .filterNotNull()
            .flatMapLatest { device ->
                getObserveListUseCase(device.ip!!, device.puerto!!)
            }
            .map { list ->
                Log.d(TAG, "VM_REPRO → Lista recibida del UseCase: ${list.size} elementos")
                list
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Selecciona un dispositivo para trabajar con él.
     *
     * @param device Tablet seleccionada desde la UI.
     */
    fun selectDevice(device: DispositivoUi) {
        viewModelScope.launch {
            Log.d(TAG, "VM_SELECT → Dispositivo seleccionado: $device")
            _selectedDevice.value = device
        }
    }

    // ---------------------------------------------------------
    //  LISTA DE ARCHIVOS PENDIENTES
    // ---------------------------------------------------------

    /**
     * Lista de archivos pendientes de enviar.
     *
     * Flujo:
     * - Observa la BD en tiempo real.
     * - Loguea cada actualización.
     */
    val pendingMedia = getAllPendingMediaUseCase()
        .map { list ->
            Log.d(TAG, "VM_PENDING → Lista de pendientes: ${list.size}")
            list
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Añade un archivo a la cola de pendientes.
     */
    fun addMedia(media: PendingMedia) {
        Log.d(TAG, "VM_PENDING → Añadiendo media: $media")
        viewModelScope.launch { addPendingMediaUseCase(media) }
    }

    /**
     * Envía el siguiente archivo pendiente (modo no cifrado).
     *
     * Advertencia:
     * - Este método ya no se usa cuando se activa el envío cifrado.
     */
    fun enviarMedia() {
        viewModelScope.launch {
            val next = getNextPendingMediaUseCase()
            Log.d(TAG, "VM_PENDING → Siguiente media para enviar: $next")

            if (next != null) {
                enviarAlTablet(next)
                updatePendingMediaStatusUseCase(next.id, PendingStatus.SENT)
                Log.d(TAG, "VM_PENDING → Media marcada como enviada")
            }
        }
    }

    /**
     * Simulación del envío no cifrado.
     */
    private suspend fun enviarAlTablet(media: PendingMedia): Boolean {
        Log.d(TAG, "VM_ACTION → Enviando al tablet: $media")
        return true
    }

    // ---------------------------------------------------------
    //  LISTA DE DISPOSITIVOS
    // ---------------------------------------------------------

    /**
     * Lista de dispositivos guardados en la BD local.
     *
     * - Se mapean a `DispositivoUi` para la UI.
     * - Se loguea cada actualización.
     */
    val dispositivos: StateFlow<List<DispositivoUi>> =
        getDispositivosUseCase()
            .map { list ->
                Log.d(TAG, "VM_DEVICES → Dispositivos desde domain: ${list.size}")
                list.map { it.toDispositivoUi() }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteDispositivo(dispositivo: Dispositivo) {
        Log.d(TAG, "VM_ACTION → Eliminando dispositivo: $dispositivo")
        viewModelScope.launch { deleteDispositivoUseCase(dispositivo) }
    }

    fun updateDispositivo(dispositivo: Dispositivo) {
        Log.d(TAG, "VM_ACTION → Actualizando dispositivo: $dispositivo")
        viewModelScope.launch { updateDispositivoUseCase(dispositivo) }
    }

    // ---------------------------------------------------------
    //  ONLINE / OFFLINE
    // ---------------------------------------------------------

    /**
     * Comprueba si una tablet está online mediante `tryLocate()`.
     *
     * @return true si responde al ping, false si está offline.
     */
    private suspend fun isOnline(ip: String, port: Int): Boolean {
        return withContext(Dispatchers.IO) {
            val result = tryLocate(ip, port) != null
            Log.d(TAG, "VM_ONLINE → Ping a $ip:$port → ${if (result) "ONLINE" else "OFFLINE"}")
            result
        }
    }

    // ---------------------------------------------------------
    //  TICKER (cada 5s)
    // ---------------------------------------------------------

    /**
     * Emite un tick cada 5 segundos para refrescar el estado ONLINE/OFFLINE.
     */
    private val ticker = flow {
        while (true) {
            Log.d(TAG, "VM_TICKER → Tick emitido")
            emit(Unit)
            kotlinx.coroutines.delay(5000)
        }
    }

    // ---------------------------------------------------------
    //  DISPOSITIVOS CON ESTADO
    // ---------------------------------------------------------

    /**
     * Combina:
     * - lista de dispositivos
     * - ticker cada 5s
     *
     * Para actualizar ONLINE/OFFLINE en tiempo real.
     */
    val dispositivosConEstado: StateFlow<List<DispositivoUi>> =
        combine(dispositivos, ticker) { lista, _ ->

            Log.d(TAG, "VM_DEVICES → Combinando dispositivos + ticker")

            if (lista.isEmpty()) {
                Log.d(TAG, "VM_DEVICES → Lista vacía, devolviendo emptyList()")
                return@combine emptyList()
            }

            lista.map { dispositivo ->
                Log.d(TAG, "VM_ONLINE → Comprobando estado de: ${dispositivo.nombre}")

                val online = isOnline(dispositivo.ip ?: "", dispositivo.puerto ?: 0)

                val actualizado = dispositivo.copy(
                    estado = if (online) EstadoDispositivo.ONLINE else EstadoDispositivo.OFFLINE,
                    icono = if (online) R.drawable.ic_tablet else R.drawable.ic_table_disabled
                )

                Log.d(TAG, "VM_ONLINE → Estado actualizado: $actualizado")
                actualizado
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------------------------------------------------------
    //  mDNS DISCOVERY
    // ---------------------------------------------------------

    /**
     * Inicia el descubrimiento mDNS para detectar tablets en la red.
     */
    fun startMdnsDiscovery() {
        viewModelScope.launch {
            Log.d(TAG, "VM_MDNS → Iniciando descubrimiento mDNS…")

            discovermDNSTabletUseCase().collect { info ->
                Log.d(TAG, "VM_MDNS → Tablet encontrada por mDNS: $info")
                _mdnsState.value = info
            }
        }
    }

    // ---------------------------------------------------------
    //  ENVÍO CIFRADO
    // ---------------------------------------------------------

    /**
     * Envía el siguiente archivo pendiente usando cifrado AES/GCM.
     *
     * Flujo:
     * 1. Obtiene el siguiente pending.
     * 2. Comprueba que hay un dispositivo seleccionado.
     * 3. Llama al UseCase `SendEncryptedMediaUseCase`.
     * 4. Si todo va bien → marca como SENT.
     *
     * @param userKey Clave introducida por el usuario para cifrar.
     */
    fun enviarMediaCifrado(userKey: String) {
        viewModelScope.launch {
            val next = getNextPendingMediaUseCase()
            if (next != null && selectedDevice.value != null) {

                val device = selectedDevice.value!!
                val ok = sendEncryptedMediaUseCase(
                    media = next,
                    ip = device.ip!!,
                    port = device.puerto!!,
                    userKey = userKey
                )

                if (ok) {
                    updatePendingMediaStatusUseCase(next.id, PendingStatus.SENT)
                } else {
                    Log.e(TAG, "VM_SEND → Error enviando media, se mantiene en cola")
                }
            }
        }
    }
}