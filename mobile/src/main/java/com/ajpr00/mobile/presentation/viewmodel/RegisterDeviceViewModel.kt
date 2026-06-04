package com.ajpr00.mobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.usecase.dispositivo.AddDispositivoUseCase
import com.ajpr00.core.domain.usecase.dispositivo.GetInfoIpUseCase
import com.ajpr00.core.domain.usecase.dispositivo.LocateAutoIpByLanUseCase
import com.ajpr00.core.domain.usecase.dispositivo.LocateTabletByManualUseCase
import com.ajpr00.core.util.isValidIpv4
import com.ajpr00.data.mapper.dispositivo.toDispositivo
import com.ajpr00.mobile.presentation.state.RegisterDeviceState
import com.ajpr00.presentation_common.state.Estado
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * RegisterDeviceViewModel
 * -----------------------
 * Este ViewModel controla toda la lógica de la pantalla de registro de dispositivos.
 *
 * Explicado para estudiantes de DAM:
 * - Piensa en el ViewModel como "el cerebro" de la pantalla.
 * - La UI (Compose) solo muestra cosas. No piensa.
 * - El ViewModel guarda el estado, reacciona a eventos y llama a los UseCases.
 *
 * Flujo general:
 * 1. El usuario pulsa un botón → la UI llama a una función del ViewModel.
 * 2. El ViewModel actualiza el estado (StateFlow).
 * 3. La UI se vuelve a dibujar automáticamente.
 * 4. Si hace falta lógica de negocio (buscar tablets, validar datos, etc),
 *    el ViewModel llama a los UseCases.
 *
 * Aquí también añadimos LOGs para seguir el flujo en Logcat.
 */

@HiltViewModel
class RegisterDeviceViewModel @Inject constructor(
    private val addDispositivoUseCase: AddDispositivoUseCase,
    private val locateAutoIpByLanUseCase: LocateAutoIpByLanUseCase,
    private val locateTabletByManualUseCase: LocateTabletByManualUseCase,
    private val getInfoIpUseCase: GetInfoIpUseCase

) : ViewModel() {

    private val TAG = "RegisterDeviceVM"

    private val _state = MutableStateFlow<Estado>(Estado.Inicial)
    val state: StateFlow<Estado> = _state

    private val _eventos = MutableSharedFlow<String>()
    val eventos = _eventos

    private val _uiState = MutableStateFlow(RegisterDeviceState())
    val uiState: StateFlow<RegisterDeviceState> = _uiState

    private var currentDevice: Dispositivo? = null

    fun isSearchingAut(value: Boolean) {
        _uiState.update { it.copy(isSearchingAut = value) }
    }

    /**
     * Alterna si se muestran o no los campos manuales.
     * Esto es típico en apps: pulsas un botón y cambia el estado.
     */
    fun onToggleManualFields() {
        Log.d(TAG, "Toggling manual fields")
        _uiState.update { it.copy(showManualFields = !it.showManualFields) }
    }

    /**
     * Actualiza el nombre introducido por el usuario.
     * Cada vez que cambia, validamos el formulario.
     */
    fun onNombreChange(value: String) {
        Log.d(TAG, "Nombre cambiado: $value")
            _uiState.update { it.copy(nombre = value) }

    }

    /**
     * Actualiza la IP introducida manualmente.
     */
    fun onIpChange(value: String) {
        Log.d(TAG, "IP cambiada: $value")

        _uiState.update { it.copy(ip = value) }
        validate()
        Log.d(TAG, "Validación finalizada _isValid: ${_uiState.value.isValid}")

    }

    /**
     * Muestra u oculta el diálogo de búsqueda.
     */
    fun showBuscarDialog(show: Boolean) {
        Log.d(TAG, "Mostrar diálogo de búsqueda: $show")
        _uiState.update { it.copy(showListAutoLan = show) }
    }

    /**
     * Busca tablets en la red LAN.
     *
     * Explicación DAM:
     * - viewModelScope.launch → lanza una corrutina (hilo ligero).
     * - locateTabletByLanUseCase(8080) devuelve un Flow.
     * - collect → se ejecuta cada vez que el Flow emite una tablet encontrada.
     * - Vamos actualizando la lista de dispositivos encontrados.
     */
    fun buscarAutoEnRed() {
        Log.d(TAG, "Iniciando búsqueda en la red...")

        viewModelScope.launch {

            // Indicamos a la UI que estamos buscando
            _uiState.update { it.copy(isSearchingAut = true, foundDevices = emptyList(), showListManualLan = false, showManualFields = false)}

            // Llamamos al UseCase que escanea la red
            locateAutoIpByLanUseCase(8080).collect { ip ->

                // Llamamos al UseCase que obtiene información de la IP
                val deviceInfo = getInfoIpUseCase(ip, 8080)

                if (deviceInfo == null) {
                    Log.d(TAG, "No se ha podido obtener información de la IP $ip")
                    return@collect
                }
                val tablet = deviceInfo.toDispositivo(ip, 8080)

                Log.d(TAG, "Tablet encontrada: $ip")

                _uiState.update { state ->
                    state.copy(
                        foundDevices = state.foundDevices + tablet
                    )
                }
            }

            Log.d(TAG, "Búsqueda finalizada")
            _uiState.update { it.copy(isSearchingAut = false) }
        }
    }

    fun buscarManual() {
        Log.d(TAG, "Iniciando búsqueda manual...")
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingManual = true, foundDevices = emptyList(), showListManualLan = true, isSearchingAut = false) }

            if (!isValidIpv4(_uiState.value.ip)) {
                enviarEvento("IP no válida")
                _uiState.update { it.copy(isSearchingManual = false, showListManualLan = false, isSearchingAut = false)}
                return@launch
            }

            val ip = _uiState.value.ip
            val port = 8080

            Log.d(TAG, "Buscando tablet en $ip:$port")

            val ipResult = locateTabletByManualUseCase(ip, port)

            if (ipResult != null) {

                val deviceInfo = getInfoIpUseCase(ipResult, 8080)

                if (deviceInfo == null) {
                    Log.d(TAG, "No se ha podido obtener información de la IP $ip")
                    enviarEvento("No se ha podido obtener información de la IP $ip")
                    return@launch
                }

                val tablet = deviceInfo.toDispositivo(ipResult, 8080)

                enviarEvento("Tablet encontrada en $ipResult")

                Log.d(TAG, "Tablet encontrada: $tablet")
                _uiState.update { it.copy(foundDevices = listOf(tablet), isSearchingManual = false, showListManualLan = true) }
            } else {
                Log.d(TAG, "No se ha encontrado la tablet")
                _uiState.update { it.copy(isSearchingManual = false, showListManualLan = false, isSearchingAut = false) }
                enviarEvento("No se ha encontrado la tablet")
            }
        }
    }

    /**
     * Valida el formulario.
     * Reglas simples:
     * - nombre no vacío
     * - ip no vacía
     */
    private fun validate() {
        _uiState.update {
            it.copy(isValid = true)
        }
        Log.d(TAG, "Validación finalizada _isValid: ${_uiState.value.isValid}")
    }

    /**
     * Resetea todo el estado a valores iniciales.
     */
    fun reset() {
        Log.d(TAG, "Reseteando estado del formulario")
        _uiState.value = RegisterDeviceState()
    }

    fun setCurrentDevice(device: Dispositivo?) {
        currentDevice = device
        validate()
        Log.d(TAG, "Dispositivo seleccionado: $device")
    }

    fun registrarDispositivo() {
        currentDevice?.let { device ->
            viewModelScope.launch {
                addDispositivoUseCase(device)
                enviarEvento("Dispositivo registrado correctamente")
            }
        } ?: run { enviarEvento("No se ha seleccionado un dispositivo")}
    }

    fun enviarEvento(mensaje: String) {
        viewModelScope.launch {
            _eventos.emit(mensaje)
        }
    }

    fun showError(message: String) {
        enviarEvento("Error: $message")
    }
}

