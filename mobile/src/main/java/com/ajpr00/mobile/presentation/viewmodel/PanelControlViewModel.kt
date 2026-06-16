package com.ajpr00.mobile.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.model.EstadoDispositivo
import com.ajpr00.core.domain.model.PendingMedia
import com.ajpr00.core.domain.model.PendingStatus
import com.ajpr00.core.domain.model.RemoteMedia
import com.ajpr00.core.domain.model.mDNS.MdnsServiceInfo
import com.ajpr00.core.domain.usecase.media.DeletePendingMediaUseCase
import com.ajpr00.data.repository.tablet.DeleteDispositivoUseCase
import com.ajpr00.data.repository.tablet.DiscovermDNSTabletUseCase
import com.ajpr00.data.repository.tablet.GetDispositivosUseCase
import com.ajpr00.data.repository.tablet.UpdateDispositivoUseCase
import com.ajpr00.core.domain.usecase.media.GetAllPendingMediaUseCase
import com.ajpr00.core.domain.usecase.media.GetFlowRemoteMediaWithThumbnailsUseCase
import com.ajpr00.core.domain.usecase.media.GetNextPendingMediaUseCase
import com.ajpr00.core.domain.usecase.media.SendEncryptedMediaUseCase
import com.ajpr00.core.domain.usecase.media.UpdatePendingMediaStatusUseCase
import com.ajpr00.core.domain.usecase.network.IsTabletAliveUseCase
import com.ajpr00.core.domain.usecase.preference.GetAesKeyUseCase
import com.ajpr00.core.util.VisumException
import com.ajpr00.data.useCase.AddPendingMediaUseCase
import com.ajpr00.mobile.presentation.state.StatePanelControl
import com.ajpr00.mobile.ui.mapper.toDispositivoUi
import com.ajpr00.mobile.ui.model.DispositivoUi
import com.ajpr00.visumloop.mobile.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

/**
 * ## PanelControlViewModel
 *
 * ViewModel principal de la pantalla de control del móvil.
 *
 * ### Responsabilidad dentro de la arquitectura
 * - **Presentation layer**: expone estado y eventos para Compose.
 * - Orquesta UseCases de dominio sin tocar infraestructura directamente.
 * - No realiza parseo ni acceso directo a Retrofit/Room.
 *
 * ### Qué controla
 * - Dispositivo seleccionado (tablet).
 * - Lista de media remota con thumbnails.
 * - Cola de `PendingMedia` a enviar.
 * - Estado ONLINE/OFFLINE de tablets.
 * - Descubrimiento mDNS.
 * - Envío cifrado de archivos al servidor NanoHTTPD.
 *
 * ### Notas
 * - Todo el trabajo pesado se hace en coroutines.
 * - Los errores de dominio se representan con `VisumException`.
 */
@HiltViewModel
class PanelControlViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getDispositivosUseCase: GetDispositivosUseCase,
    private val deleteDispositivoUseCase: DeleteDispositivoUseCase,
    private val updateDispositivoUseCase: UpdateDispositivoUseCase,
    private val addPendingMediaUseCase: AddPendingMediaUseCase,
    private val getNextPendingMediaUseCase: GetNextPendingMediaUseCase,
    private val deletePendingMediaUseCase: DeletePendingMediaUseCase,
    private val updatePendingMediaStatusUseCase: UpdatePendingMediaStatusUseCase,
    private val getAllPendingMediaUseCase: GetAllPendingMediaUseCase,
    private val getObserveListUseCase: GetFlowRemoteMediaWithThumbnailsUseCase,
    private val discovermDNSTabletUseCase: DiscovermDNSTabletUseCase,
    private val sendEncryptedMediaUseCase: SendEncryptedMediaUseCase,
    private val isTabletAliveUseCase: IsTabletAliveUseCase,
    private val getAesKeyUseCase: GetAesKeyUseCase
) : ViewModel() {

    private val TAG = "PanelControlVM"

    private val _state = MutableStateFlow<StatePanelControl>(StatePanelControl())
    val state: StateFlow<StatePanelControl> = _state

    private val _eventos = MutableSharedFlow<String>()
    val eventos: SharedFlow<String> = _eventos.asSharedFlow()

    // ---------------------------------------------------------
    //  DISPOSITIVO SELECCIONADO
    // ---------------------------------------------------------

    /**
     * Dispositivo seleccionado actualmente.
     *
     * - `null` → no hay tablet seleccionada.
     * - `DispositivoUi` → tablet activa.
     */
    private val _selectedDevice = MutableStateFlow<DispositivoUi?>(null)
    val selectedDevice: StateFlow<DispositivoUi?> = _selectedDevice

    /**
     * Último servicio mDNS detectado.
     */
    private val _mdnsState = MutableStateFlow<MdnsServiceInfo?>(null)

    // ---------------------------------------------------------
    //  LISTA REMOTA DE MEDIA (STREAMING)
    // ---------------------------------------------------------

    /**
     * Lista de media remota obtenida desde la tablet seleccionada.
     *
     * Flujo:
     * 1. Espera a que haya dispositivo seleccionado.
     * 2. Observa media + thumbnails en tiempo real.
     * 3. Loguea tamaño de lista para depuración.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val listReproServer: StateFlow<List<RemoteMedia>> =
        selectedDevice
            .filterNotNull()
            .flatMapLatest { device ->
                getObserveListUseCase(device.ip!!, device.puerto!!)
            }
            .map { list ->
                Log.d(TAG, "VM_REPRO → Lista recibida: ${list.size} elementos")
                list
            }.catch { e ->
                Log.e(TAG, "VM_REPRO_ERROR → ${e.message}")
                enviarEvento("Error al obtener lista de reproducción")
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(2000), emptyList())

    /**
     * Selecciona un dispositivo para trabajar con él.
     */
    fun selectDevice(device: DispositivoUi) {
        Log.d(TAG, "VM_SELECT → Dispositivo seleccionado: $device")
        _selectedDevice.value = device
    }

    fun closeExplorer() {
        _state.value = _state.value.copy(showExplorerFav = false)
    }

    fun loadListFav() {
        _state.value = _state.value.copy(showExplorerFav = true)
    }

    // ---------------------------------------------------------
    //  LISTA DE ARCHIVOS PENDIENTES
    // ---------------------------------------------------------

    /**
     * Lista de archivos pendientes de enviar.
     *
     * Observa la BD en tiempo real y loguea cada actualización.
     */
    val pendingMedia: StateFlow<List<PendingMedia>> =
        getAllPendingMediaUseCase()
            .map { list ->
                Log.d(TAG, "VM_PENDING → Lista de pendientes: ${list.size}")
                list
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Importa las URIs seleccionadas y las añade como `PendingMedia`.
     *
     * Flujo:
     * 1. Comprueba que hay dispositivo seleccionado.
     * 2. Llama al UseCase de importación.
     * 3. Emite eventos de éxito o error.
     */
    fun importSelectedMedia(uris: List<Uri>) {
        viewModelScope.launch {
            try {
                val deviceId = selectedDevice.value?.id
                    ?: throw IllegalStateException("No hay dispositivo seleccionado")

                addPendingMediaUseCase(uris, deviceId, context)
                enviarEvento("Medias importadas correctamente")

            } catch (e: VisumException) {
                Log.e(TAG, "VM_IMPORT_ERROR → ${e.code}")
                showError("Error al importar medias: ${e.code}")

            } catch (e: IllegalStateException) {
                Log.e(TAG, "VM_IMPORT_ERROR_STATE → ${e.message}")
                showError(e.message ?: "Error al importar medias")

            } catch (e: Exception) {
                Log.e(TAG, "VM_IMPORT_ERROR_UNEXPECTED → ${e.message}")
                showError("Error inesperado al importar medias")
            }
        }
    }

    /**
     * Envía el siguiente archivo pendiente (modo antiguo no cifrado).
     *
     * Se mantiene por compatibilidad, pero el flujo recomendado es el cifrado.
     */
    fun enviarMedia() {
        viewModelScope.launch {
            try {
                val next = getNextPendingMediaUseCase()
                Log.d(TAG, "VM_PENDING → Siguiente media para enviar: $next")

                if (next != null) {
                    enviarAlTablet(next)
                    updatePendingMediaStatusUseCase(next.id, PendingStatus.SENT)
                    Log.d(TAG, "VM_PENDING → Media marcada como enviada")
                }
            } catch (e: VisumException) {
                Log.e(TAG, "VM_SEND_ERROR → ${e.code}")
                showError("Error al enviar media: ${e.code}")
            } catch (e: Exception) {
                Log.e(TAG, "VM_SEND_ERROR_UNEXPECTED → ${e.message}")
                showError("Error inesperado al enviar media")
            }
        }
    }

    /**
     * Simulación del envío no cifrado.
     */
    private suspend fun enviarAlTablet(media: PendingMedia): Boolean {
        Log.d(TAG, "VM_ACTION → Enviando al tablet (simulado): $media")
        return true
    }

    /**
     * Envía el siguiente archivo pendiente usando cifrado AES/GCM.
     *
     * Flujo:
     * 1. Obtiene el siguiente pending.
     * 2. Comprueba que hay dispositivo seleccionado.
     * 3. Obtiene la clave AES.
     * 4. Llama a `SendEncryptedMediaUseCase`.
     * 5. Marca como SENT si todo va bien.
     */
    fun enviarMediaCifrado() {
        viewModelScope.launch {
            try {
              while (pendingMedia.value.isNotEmpty())  {val next = getNextPendingMediaUseCase()
                    ?: run {
                        Log.d(TAG, "VM_ENCRYPT → No hay media pendiente")
                        return@launch
                    }

                val device = selectedDevice.value
                    ?: run {
                        Log.d(TAG, "VM_ENCRYPT → No hay dispositivo seleccionado")
                        return@launch
                    }

                val aesKey = getAesKeyUseCase()
                    ?: run {
                        Log.d(TAG, "VM_ENCRYPT → No hay clave AES disponible")
                        return@launch
                    }

                sendEncryptedMediaUseCase(
                    media = next,
                    ip = device.ip!!,
                    port = device.puerto!!,
                    aesKey = aesKey
                )

                Log.d(
                    TAG,
                    "VM_ENCRYPT → Media enviada correctamente id=${next.id}, device=${device.id}"
                )

                deletePendingMediaUseCase(next)
                enviarEvento("Media enviada correctamente")
            }
            } catch (e: VisumException) {
                Log.e(TAG, "VM_ENCRYPT_ERROR → ${e.code}")
                enviarEvento("Error al enviar media: ${e.code}")

            } catch (e: Exception) {
                Log.e(TAG, "VM_ENCRYPT_ERROR_UNEXPECTED → ${e.message}")
                enviarEvento("Error inesperado al enviar media")
            }
        }
    }

    // ---------------------------------------------------------
    //  LISTA DE DISPOSITIVOS
    // ---------------------------------------------------------

    /**
     * Lista de dispositivos guardados en la BD local.
     *
     * Se mapean a `DispositivoUi` para la UI y se loguea cada actualización.
     */
    private val dispositivos: StateFlow<List<DispositivoUi>> =
        getDispositivosUseCase()
            .map { list ->
                Log.d(TAG, "VM_DEVICES → Dispositivos desde domain: ${list.size}")
                list.map { it.toDispositivoUi() }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Elimina un dispositivo de la BD.
     */
    fun deleteDispositivo(dispositivo: Dispositivo) {
        Log.d(TAG, "VM_ACTION → Eliminando dispositivo: $dispositivo")
        viewModelScope.launch {
            try {
                deleteDispositivoUseCase(dispositivo)
            } catch (e: VisumException) {
                Log.e(TAG, "VM_DELETE_ERROR → ${e.code}")
                showError("Error al eliminar dispositivo: ${e.code}")
            } catch (e: Exception) {
                Log.e(TAG, "VM_DELETE_ERROR_UNEXPECTED → ${e.message}")
                showError("Error inesperado al eliminar dispositivo")
            }
        }
    }

    /**
     * Actualiza un dispositivo en la BD.
     */
    fun updateDispositivo(dispositivo: Dispositivo) {
        Log.d(TAG, "VM_ACTION → Actualizando dispositivo: $dispositivo")
        viewModelScope.launch {
            try {
                updateDispositivoUseCase(dispositivo)
            } catch (e: VisumException) {
                Log.e(TAG, "VM_UPDATE_ERROR → ${e.code}")
                showError("Error al actualizar dispositivo: ${e.code}")
            } catch (e: Exception) {
                Log.e(TAG, "VM_UPDATE_ERROR_UNEXPECTED → ${e.message}")
                showError("Error inesperado al actualizar dispositivo")
            }
        }
    }

    // ---------------------------------------------------------
    //  ONLINE / OFFLINE
    // ---------------------------------------------------------

    // ---------------------------------------------------------
    //  TICKER (cada 5s)
    // ---------------------------------------------------------

    /**
     * Emite un tick cada 5 segundos para refrescar el estado ONLINE/OFFLINE.
     */
    private val ticker = flow {
        while (true) {
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

            if (lista.isEmpty()) return@combine emptyList()

            supervisorScope {
                lista.map { dispositivo ->
                    async(Dispatchers.IO) {

                        val online = runCatching {
                            isTabletAliveUseCase(
                                dispositivo.ip.orEmpty(),
                                dispositivo.puerto ?: 0
                            ).getOrDefault(false)
                        }
                            .onFailure {
                                Log.w(
                                    TAG,
                                    "VM_ONLINE → Error en ${dispositivo.ip}: ${it.message}"
                                )
                            }
                            .getOrDefault(false)

                        dispositivo.copy(
                            estado = if (online)
                                EstadoDispositivo.ONLINE
                            else
                                EstadoDispositivo.OFFLINE,
                            icono = if (online)
                                R.drawable.ic_tablet
                            else
                                R.drawable.ic_table_disabled
                        )
                    }
                }.awaitAll()
            }
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    // ---------------------------------------------------------
    //  EVENTOS UI
    // ---------------------------------------------------------

    /**
     * Emite un evento de un solo uso hacia la UI (snackbar, diálogo, etc).
     */
    private fun enviarEvento(mensaje: String) {
        viewModelScope.launch {
            _eventos.emit(mensaje)
        }
    }

    /**
     * Muestra un error formateado hacia la UI.
     */
    fun showError(message: String) {
        enviarEvento("Error: $message")
    }
}