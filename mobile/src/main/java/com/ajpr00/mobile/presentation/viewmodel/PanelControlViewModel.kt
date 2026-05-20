package com.ajpr00.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.model.PendingMedia
import com.ajpr00.core.domain.model.PendingStatus
import com.ajpr00.core.domain.usecase.AddDispositivoUseCase
import com.ajpr00.core.domain.usecase.AddPendingMediaUseCase
import com.ajpr00.core.domain.usecase.DeleteDispositivoUseCase
import com.ajpr00.core.domain.usecase.GetAllPendingMediaUseCase
import com.ajpr00.core.domain.usecase.GetDispositivosUseCase
import com.ajpr00.core.domain.usecase.GetNextPendingMediaUseCase
import com.ajpr00.core.domain.usecase.UpdateDispositivoUseCase
import com.ajpr00.core.domain.usecase.UpdatePendingMediaStatusUseCase
import com.ajpr00.mobile.ui.mapper.toDispositivoUi
import com.ajpr00.mobile.ui.model.DispositivoUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PanelControlViewModel @Inject constructor(
    // UseCases del dominio: aquí no hay lógica, solo llamadas limpias a la capa domain.
    private val getDispositivosUseCase: GetDispositivosUseCase,
    private val addDispositivoUseCase: AddDispositivoUseCase,
    private val deleteDispositivoUseCase: DeleteDispositivoUseCase,
    private val updateDispositivoUseCase: UpdateDispositivoUseCase,

    // UseCases para gestionar la cola de archivos pendientes de enviar.
    private val addPendingMediaUseCase: AddPendingMediaUseCase,
    private val getNextPendingMediaUseCase: GetNextPendingMediaUseCase,
    private val updatePendingMediaStatusUseCase: UpdatePendingMediaStatusUseCase,
    private val getAllPendingMediaUseCase: GetAllPendingMediaUseCase,
   // private val getListaReposicionUseCase: GetListaReposicionUseCase

) : ViewModel() {

    // ---------------------------------------------------------
    //  SELECCIÓN DE DISPOSITIVO
    // ---------------------------------------------------------
    // Aquí guardamos qué dispositivo ha seleccionado el usuario.
    // Es un StateFlow porque la UI necesita reaccionar cuando cambia.
    private val _selectedDevice = MutableStateFlow<DispositivoUi?>(null)
    val selectedDevice: StateFlow<DispositivoUi?> = _selectedDevice

    // ---------------------------------------------------------
    //  LISTA DE ARCHIVOS PENDIENTES DE ENVÍO
    // ---------------------------------------------------------
    // Esto escucha la BD en tiempo real. Cada vez que se añade o cambia
    // un archivo pendiente, la UI se actualiza sola.
    val pendingMedia = getAllPendingMediaUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Añadir un archivo a la cola de envío.
    // Aquí no enviamos nada todavía, solo lo guardamos en BD.
    fun addMedia(media: PendingMedia) {
        viewModelScope.launch {
            addPendingMediaUseCase(media)
        }
    }

    // ---------------------------------------------------------
    //  ENVÍO DE ARCHIVOS AL TABLET
    // ---------------------------------------------------------
    // Esta función busca el siguiente archivo pendiente y lo envía.
    // Si todo va bien → lo marcamos como SENT.
    // Si falla → en el futuro podríamos marcarlo como FAILED o reintentar.
    fun enviarMedia() {
        viewModelScope.launch {
            val next = getNextPendingMediaUseCase()

            if (next != null) {
                // Aquí llamamos a la función que realmente hace el envío.
                enviarAlTablet(next)

                // Si llegamos aquí, asumimos que se envió bien.
                updatePendingMediaStatusUseCase(next.id, PendingStatus.SENT)
            }
        }
    }

    // ---------------------------------------------------------
    //  SELECCIONAR DISPOSITIVO
    // ---------------------------------------------------------
    // Cuando el usuario toca un dispositivo en la UI, lo guardamos aquí.
    fun selectDevice(device: DispositivoUi) {
        _selectedDevice.value = device
    }

    // ---------------------------------------------------------
    //  LISTA DE DISPOSITIVOS
    // ---------------------------------------------------------
    // Obtenemos los dispositivos desde domain y los convertimos a UI.
    val dispositivos: StateFlow<List<DispositivoUi>> =
        getDispositivosUseCase()
            .map { list -> list.map { it.toDispositivoUi() } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // ---------------------------------------------------------
    //  CRUD DE DISPOSITIVOS
    // ---------------------------------------------------------
    fun addDispositivo(dispositivo: Dispositivo) {
        viewModelScope.launch {
            addDispositivoUseCase(dispositivo)
        }
    }

    fun deleteDispositivo(dispositivo: Dispositivo) {
        viewModelScope.launch {
            deleteDispositivoUseCase(dispositivo)
        }
    }

    fun updateDispositivo(dispositivo: Dispositivo) {
        viewModelScope.launch {
            updateDispositivoUseCase(dispositivo)
        }
    }

    // ---------------------------------------------------------
    //  ENVÍO REAL AL TABLET (AÚN SIN IMPLEMENTAR)
    // ---------------------------------------------------------
    // Esta función será la encargada de hacer la petición HTTP al tablet.
    // Aquí irá la lógica de OkHttp o Ktor para subir el archivo.
    // De momento está vacía porque la implementarás más adelante.
    private suspend fun enviarAlTablet(media: PendingMedia): Boolean {
        // Aquí irá la magia del envío LAN.
        // La idea es:
        // 1. Obtener IP y puerto del dispositivo seleccionado.
        // 2. Crear un multipart con el archivo.
        // 3. Hacer POST al servidor del tablet.
        // 4. Devolver true si todo fue bien.
        return true
    }
}
