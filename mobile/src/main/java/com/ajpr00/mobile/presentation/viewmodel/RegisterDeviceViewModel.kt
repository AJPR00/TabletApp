package com.ajpr00.mobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.model.EstadoDispositivo
import com.ajpr00.core.domain.model.Eventos
import com.ajpr00.core.domain.model.qr.QrPayload
import com.ajpr00.data.repository.tablet.AddDispositivoUseCase
import com.ajpr00.data.repository.tablet.DiscovermDNSTabletUseCase
import com.ajpr00.data.repository.tablet.GetInfoIpUseCase
import com.ajpr00.data.repository.tablet.LocateAutoIpByLanUseCase
import com.ajpr00.data.repository.tablet.LocateTabletByManualUseCase
import com.ajpr00.core.domain.usecase.pairing.CallShowPinUseCase
import com.ajpr00.core.domain.usecase.pairing.SetParinUseCase
import com.ajpr00.core.domain.usecase.preference.SaveAesKeyUseCase
import com.ajpr00.core.domain.usecase.user.IsLoginStateUseCase
import com.ajpr00.core.util.isValidIpv4
import com.ajpr00.data.mapper.tablet.toDispositivo
import com.ajpr00.data.security.decryptAesRealFromServer
import com.ajpr00.mobile.data.mapper.toDispositivo
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
 * # RegisterDeviceViewModel
 *
 * ViewModel encargado de gestionar toda la lógica de la pantalla de **registro y emparejamiento
 * de dispositivos** (tablets) dentro de la app móvil.
 *
 * ## Rol dentro de la arquitectura
 * - **Presentation layer**: expone estado inmutable para Compose.
 * - **No contiene lógica de dominio**: delega todo a los UseCases.
 * - **No toca infraestructura**: no accede a Retrofit, DataStore ni BD directamente.
 *
 * ## Qué controla este ViewModel
 * - Búsqueda automática de tablets en LAN (escaneo IP → consulta info → conversión a modelo).
 * - Búsqueda manual por IP.
 * - Descubrimiento por mDNS.
 * - Validación del formulario.
 * - Gestión del PIN de emparejamiento.
 * - Descifrado AES y registro final del dispositivo.
 *
 * ## Pipeline LAN (resumen)
 * ```
 * locateAutoIpByLanUseCase → IP detectada
 * → getInfoIpUseCase → datos del servidor
 * → toDispositivo() → modelo de dominio
 * → UIState.foundDevices
 * ```
 *
 * ## Pipeline de emparejamiento (resumen)
 * ```
 * PIN introducido por usuario
 * → setParinUseCase → salt + AES cifrada
 * → decryptAesRealFromServer() → AES real
 * → saveAesKeyUseCase() → persistencia segura
 * → addDispositivoUseCase() → BD local
 * → Eventos.RegisterSuccess
 * ```
 *
 * ## Logs
 * Se añaden logs pedagógicos para seguir el flujo en Logcat.
 */
@HiltViewModel
class RegisterDeviceViewModel @Inject constructor(
    private val addDispositivoUseCase: AddDispositivoUseCase,
    private val isLoggedInUseCase: IsLoginStateUseCase,
    private val locateAutoIpByLanUseCase: LocateAutoIpByLanUseCase,
    private val locateTabletByManualUseCase: LocateTabletByManualUseCase,
    private val discovermDNSTabletUseCase: DiscovermDNSTabletUseCase,
    private val getInfoIpUseCase: GetInfoIpUseCase,
    private val callShowPinUseCase: CallShowPinUseCase,
    private val setParinUseCase: SetParinUseCase,
    private val savekeyUseCase: SaveAesKeyUseCase
) : ViewModel() {

    private val TAG = "RegisterDeviceVM"

    /** Estado general de la pantalla (Inicial, Cargando, Error…). */
    private val _state = MutableStateFlow<Estado>(Estado.Inicial)
    val state: StateFlow<Estado> = _state

    /** Eventos de una sola emisión (snackbars, diálogos, etc.). */
    private val _eventos = MutableSharedFlow<Eventos>()
    val eventos = _eventos

    /** Estado específico de la UI (campos, listas, flags). */
    private val _uiState = MutableStateFlow(RegisterDeviceState())
    val uiState: StateFlow<RegisterDeviceState> = _uiState

    /** Datos temporales usados durante el proceso de emparejamiento. */
    var salt: String? = null
    private var currentDevice: Dispositivo? = null
    private var currentPayload: QrPayload? = null

    // -------------------------------------------------------------------------
    // SETTERS DE ESTADO
    // -------------------------------------------------------------------------

    /**
     * Guarda el payload leído desde el QR.
     *
     * ## Parámetros
     * - `payload`: datos del QR (IP, puerto, nombre, id…).
     */
    fun setCurrentPayload(payload: QrPayload) {
        Log.d(TAG, "Payload QR recibido: $payload")
        currentPayload = payload
        validate()
    }

    /** Cambia el flag de búsqueda automática. */
    fun isSearchingAut(value: Boolean) {
        _uiState.update { it.copy(isSearchingAut = value) }
    }

    /** Muestra/oculta los campos de búsqueda manual. */
    fun onToggleManualFields(value: Boolean) {
        _uiState.update { it.copy(showManualFields = value) }
    }

    /** Actualiza el nombre del dispositivo. */
    fun onNombreChange(value: String) {
        _uiState.update { it.copy(nombre = value) }
    }

    /** Actualiza la IP y valida. */
    fun onIpChange(value: String) {
        _uiState.update { it.copy(ip = value) }
    }

    /** Muestra u oculta el diálogo de dispositivos encontrados automáticamente. */
    fun showBuscarDialog(show: Boolean) {
        _uiState.update { it.copy(showListAutoLan = show, foundDevices = emptyList()) }
    }

    // -------------------------------------------------------------------------
    // BÚSQUEDA AUTOMÁTICA EN LAN
    // -------------------------------------------------------------------------

    /**
     * Búsqueda automática de tablets en la red LAN.
     *
     * ## Flujo
     * 1. Activa modo búsqueda.
     * 2. Escucha IPs encontradas por el UseCase.
     * 3. Para cada IP → obtiene info → la convierte en `Dispositivo`.
     * 4. Actualiza la lista de dispositivos encontrados.
     *
     * ## Advertencias
     * - No bloquea el hilo principal.
     * - Puede encontrar varias tablets simultáneamente.
     */
    fun buscarAutoEnRed() {
        viewModelScope.launch {
            Log.d(TAG, "Iniciando búsqueda automática en LAN")

            _uiState.update {
                it.copy(
                    isSearchingAut = true,
                    foundDevices = emptyList(),
                    showListManualLan = false,
                    showManualFields = false
                )
            }

            locateAutoIpByLanUseCase(8080).collect { ip ->
                Log.d(TAG, "IP detectada en LAN: $ip")

                val deviceInfo = getInfoIpUseCase(ip, 8080)
                if (deviceInfo == null) {
                    Log.d(TAG, "No se pudo obtener info de $ip")
                    return@collect
                }

                val tablet = deviceInfo.toDispositivo(ip, 8080)
                Log.d(TAG, "Tablet encontrada: $tablet")

                _uiState.update { state ->
                    state.copy(foundDevices = state.foundDevices + tablet)
                }
            }

            _uiState.update { it.copy(isSearchingAut = false) }
            Log.d(TAG, "Búsqueda automática finalizada")
        }
    }

    // -------------------------------------------------------------------------
    // BÚSQUEDA MANUAL POR IP
    // -------------------------------------------------------------------------

    /**
     * Búsqueda manual por IP.
     *
     * ## Flujo
     * 1. Valida la IP.
     * 2. Llama al UseCase de búsqueda manual.
     * 3. Si encuentra → obtiene info → actualiza lista.
     * 4. Si no → emite error.
     */
    /**
     * Búsqueda manual por IP.
     *
     * ## Flujo
     * 1. Valida la IP introducida por el usuario.
     * 2. Llama al UseCase de búsqueda manual.
     * 3. Si encuentra la tablet → obtiene info → actualiza lista.
     * 4. Si no → emite error y cierra el panel.
     *
     * ## Logs pedagógicos
     * Se registran todos los pasos para poder seguir el flujo completo:
     * - Inicio de búsqueda
     * - Validación de IP
     * - Resultado del UseCase
     * - Resultado de getInfoIpUseCase
     * - Actualización del estado UI
     */
    fun buscarManual() {
        viewModelScope.launch {
            Log.d(TAG, "===== BUSQUEDA MANUAL INICIADA =====")

            val ip = _uiState.value.ip
            val port = 8080

            Log.d(TAG, "IP introducida por el usuario: $ip")

            _uiState.update {
                it.copy(
                    isSearchingManual = true,
                    foundDevices = emptyList(),
                    showListManualLan = true,
                    isSearchingAut = false
                )
            }

            // 1. Validación de IP
            if (!isValidIpv4(ip)) {
                Log.e(TAG, "IP inválida: $ip")
                enviarEvento(Eventos.Error("IP no válida"))
                _uiState.update { it.copy(isSearchingManual = false, showListManualLan = false) }
                Log.d(TAG, "===== BUSQUEDA MANUAL CANCELADA POR IP INVALIDA =====")
                return@launch
            }

            Log.d(TAG, "IP válida. Intentando obtener información en $ip:$port")

            // 2. Intentar obtener info directamente
            val deviceInfo = getInfoIpUseCase(ip, port)
            Log.d(TAG, "Resultado getInfoIpUseCase: $deviceInfo")

            if (deviceInfo == null) {
                Log.e(TAG, "No se pudo obtener información de la IP $ip")
                enviarEvento(Eventos.Error("No se ha podido obtener información de la IP $ip"))
                _uiState.update { it.copy(isSearchingManual = false, showListManualLan = false) }
                Log.d(TAG, "===== BUSQUEDA MANUAL FINALIZADA SIN INFO =====")
                return@launch
            }

            Log.d(TAG, "Información recibida del servidor: $deviceInfo")

            // 3. Convertir a modelo de dominio
            val tablet = deviceInfo.toDispositivo(ip, port)
            Log.d(TAG, "Tablet convertida a modelo de dominio: $tablet")

            enviarEvento(Eventos.Info("Tablet encontrada en $ip"))

            // 4. Actualizar UIState
            _uiState.update {
                it.copy(
                    foundDevices = listOf(tablet),
                    isSearchingManual = false,
                    showListManualLan = true
                )
            }

            Log.d(TAG, "UIState actualizado: foundDevices = ${_uiState.value.foundDevices}")

            validate()

            Log.d(TAG, "===== BUSQUEDA MANUAL FINALIZADA CON EXITO =====")
        }
    }


    // -------------------------------------------------------------------------
    // BÚSQUEDA POR mDNS
    // -------------------------------------------------------------------------

    /**
     * Descubrimiento de tablets mediante mDNS.
     *
     * ## Notas
     * - mDNS detecta dispositivos anunciándose en la red.
     * - No depende de rangos IP.
     */
    fun startSearchMdns() {
        viewModelScope.launch {
            Log.d(TAG, "Iniciando búsqueda mDNS")
            discovermDNSTabletUseCase().collect { info ->
                val dispositivo = info.toDispositivo()
                _uiState.update {
                    it.copy(foundDevices = it.foundDevices + dispositivo)
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // VALIDACIÓN Y RESET
    // -------------------------------------------------------------------------

    /** Marca el formulario como válido (placeholder). */
    private fun validate() {
        _uiState.update { it.copy(isValid = true) }
    }

    /** Resetea todo el estado de la pantalla. */
    fun reset() {
        Log.d(TAG, "Reseteando estado de registro")
        _uiState.value = RegisterDeviceState()
    }

    /** Guarda el dispositivo seleccionado. */
    fun setCurrentDevice(device: Dispositivo?) {
        currentDevice = device
        validate()
    }

    // -------------------------------------------------------------------------
    // PIN Y EMPAREJAMIENTO
    // -------------------------------------------------------------------------

    /**
     * Solicita el PIN de emparejamiento al servidor de la tablet.
     */
    fun getPinParing() {
        viewModelScope.launch {
            val dispositivo = currentDevice ?: return@launch

            Log.d(TAG, "Solicitando PIN a ${dispositivo.ip}:${dispositivo.puerto}")

            val result = callShowPinUseCase(dispositivo.ip!!, dispositivo.puerto!!)
            result.onSuccess { salt ->
                this@RegisterDeviceViewModel.salt = salt
            }.onFailure { error ->
                _eventos.emit(Eventos.Error(error.message ?: "Error desconocido"))
            }
        }
    }

    fun registreDispositQr() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPairingQr = true) }

            paringDispositivo(currentPayload!!.pin)

            _uiState.update { it.copy(isPairingQr = false) }
        }
    }

    /**
     * Realiza el emparejamiento completo:
     *
     * ## Flujo
     * 1. Envía PIN al servidor.
     * 2. Recibe clave AES cifrada.
     * 3. Descifra clave AES real.
     * 4. Guarda clave AES en DataStore.
     * 5. Registra dispositivo en BD local.
     *
     * ## Modos
     * - **QR**: usa datos del payload.
     * - **Manual/Auto**: usa `currentDevice`.
     */
    fun paringDispositivo(pin: String) {
        viewModelScope.launch {

            // -------------------------
            // MODO QR
            // -------------------------
            currentPayload?.let { payload ->
                Log.d(TAG, "Emparejando dispositivo desde QR…")

                runCatching {
                    val (salt, encryptedKey) =
                        setParinUseCase(payload.ip, payload.puerto, pin).getOrThrow()

                    val aesReal = decryptAesRealFromServer(
                        pin = pin,
                        saltBase64 = salt,
                        encryptedBase64 = encryptedKey
                    )

                    savekeyUseCase(aesReal)

                    val device = Dispositivo(
                        id = payload.id,
                        nombre = payload.nombre,
                        ip = payload.ip,
                        puerto = payload.puerto,
                        estado = EstadoDispositivo.ONLINE
                    )

                    addDispositivoUseCase(device)
                    _eventos.emit(Eventos.RegisterSuccess)

                }.onFailure { error ->
                    _eventos.emit(Eventos.Error(error.message ?: "Error desconocido"))
                }

                return@launch
            }

            // -------------------------
            // MODO AUTOMÁTICO / MANUAL
            // -------------------------
            currentDevice?.let { device ->
                Log.d(TAG, "Emparejando dispositivo seleccionado manualmente…")

                runCatching {
                    val (salt, encryptedKey) =
                        setParinUseCase(device.ip!!, device.puerto!!, pin).getOrThrow()

                    val aesReal = decryptAesRealFromServer(
                        pin = pin,
                        saltBase64 = salt,
                        encryptedBase64 = encryptedKey
                    )

                    savekeyUseCase(aesReal)

                    addDispositivoUseCase(
                        device.copy(
                            aesKey = null,
                            estado = EstadoDispositivo.ONLINE
                        )
                    )

                    _eventos.emit(Eventos.RegisterSuccess)

                }.onFailure { error ->
                    _eventos.emit(Eventos.Error(error.message ?: "Error desconocido"))
                }
            }
        }
    }

    /**
     * Registra un dispositivo sin emparejamiento (modo fallback).
     */
    fun pairigDispositivo() {
        currentDevice?.let { device ->
            viewModelScope.launch {
                addDispositivoUseCase(device)
                _eventos.emit(Eventos.RegisterSuccess)
            }
        }
    }

    // -------------------------------------------------------------------------
    // EVENTOS
    // -------------------------------------------------------------------------

    /** Emite un evento hacia la UI. */
    fun enviarEvento(evento: Eventos) {
        viewModelScope.launch { _eventos.emit(evento) }
    }

    /** Emite un error hacia la UI. */
    fun showError(message: String) {
        enviarEvento(Eventos.Error(message))
    }
}