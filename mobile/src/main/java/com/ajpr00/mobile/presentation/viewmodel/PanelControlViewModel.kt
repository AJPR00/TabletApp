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
import com.ajpr00.core.domain.usecase.media.UpdatePendingMediaStatusUseCase
import com.ajpr00.core.util.tryLocate
import com.ajpr00.mobile.ui.mapper.toDispositivoUi
import com.ajpr00.mobile.ui.model.DispositivoUi
import com.ajpr00.visumloop.mobile.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private val discovermDNSTabletUseCase: DiscovermDNSTabletUseCase
) : ViewModel() {
    private val TAG = "PanelControlVM"

    // ---------------------------------------------------------
//  DISPOSITIVO SELECCIONADO
// ---------------------------------------------------------
    private val _selectedDevice = MutableStateFlow<DispositivoUi?>(null)
    val selectedDevice: StateFlow<DispositivoUi?> = _selectedDevice

    private val _mdnsState = MutableStateFlow<MdnsServiceInfo?>(null)
    val mdnsState: StateFlow<MdnsServiceInfo?> = _mdnsState

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
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    fun selectDevice(device: DispositivoUi) {
        viewModelScope.launch {
            Log.d(TAG, "VM_SELECT → Dispositivo seleccionado: $device")

            _selectedDevice.value = device
        }
    }

    // ---------------------------------------------------------
//  LISTA DE ARCHIVOS PENDIENTES
// ---------------------------------------------------------
    val pendingMedia = getAllPendingMediaUseCase()
        .map { list ->
            Log.d(TAG, "VM_PENDING → Lista de pendientes: ${list.size}")
            list
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMedia(media: PendingMedia) {
        Log.d(TAG, "VM_PENDING → Añadiendo media: $media")
        viewModelScope.launch { addPendingMediaUseCase(media) }
    }

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

    private suspend fun enviarAlTablet(media: PendingMedia): Boolean {
        Log.d(TAG, "VM_ACTION → Enviando al tablet: $media")
        return true
    }

    // ---------------------------------------------------------
//  LISTA DE DISPOSITIVOS
// ---------------------------------------------------------
    val dispositivos: StateFlow<List<DispositivoUi>> =
        getDispositivosUseCase()
            .map { list ->
                Log.d(TAG, "VM_DEVICES → Dispositivos desde domain: ${list.size}")
                list.map { it.toDispositivoUi() }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

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
    private suspend fun isOnline(ip: String, port: Int): Boolean {
        return withContext(Dispatchers.IO) {
            val result = tryLocate(ip, port) != null
            Log.d(TAG, "VM_ONLINE → Ping a $ip:$port → ${if (result) "ONLINE" else "OFFLINE"}")
            result
        }
    }

    // ---------------------------------------------------------
//  TICKER
// ---------------------------------------------------------
    private val ticker = kotlinx.coroutines.flow.flow {
        while (true) {
            Log.d(TAG, "VM_TICKER → Tick emitido")
            emit(Unit)
            kotlinx.coroutines.delay(5000)
        }
    }

    // ---------------------------------------------------------
//  DISPOSITIVOS CON ESTADO
// ---------------------------------------------------------
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
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun startMdnsDiscovery() {
        viewModelScope.launch {
            Log.d(TAG, "VM_MDNS → Iniciando descubrimiento mDNS…")

            discovermDNSTabletUseCase().collect { info ->
                Log.d(TAG, "VM_MDNS → Tablet encontrada por mDNS: $info")
                _mdnsState.value = info
            }
        }
    }

}
