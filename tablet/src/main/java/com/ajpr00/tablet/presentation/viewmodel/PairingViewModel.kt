package com.ajpr00.tablet.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.model.qr.QrPayload
import com.ajpr00.core.domain.usecase.pairing.ObservePinUseCase
import com.ajpr00.core.domain.usecase.pairing.ShowPinUseCase
import com.ajpr00.core.domain.usecase.pairing.GetQrUseCase
import com.ajpr00.core.domain.usecase.preference.setting.SaveAesKeyUseCase
import com.ajpr00.tablet.presentation.state.PairingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * # PairingViewModel
 *
 * ViewModel responsable de gestionar todo el flujo de emparejamiento entre
 * **tablet → móvil**, incluyendo:
 *
 * - Solicitud del PIN (`/show_pin`)
 * - Observación del PIN emitido por el servidor
 * - Generación del QR (`/show_qr`)
 * - Exposición del estado de UI mediante [PairingState]
 *
 * ## Responsabilidades principales
 * - Coordinar los *use cases* de emparejamiento.
 * - Mantener el estado de la UI reactivo mediante `StateFlow`.
 * - Exponer el objeto [QrPayload] para que la UI genere el QR.
 *
 * ## Relación con otras capas
 * - **domain/pairing** → `ShowPinUseCase`, `ObservePinUseCase`, `ShowQrUseCase`
 * - **presentation** → `PairingState` y pantallas de emparejamiento
 *
 * ## Notas para estudiantes DAM
 * - Este ViewModel NO contiene lógica de red.
 * - Solo orquesta *use cases* y actualiza estado.
 * - Los logs explican el flujo paso a paso.
 */
@HiltViewModel
class PairingViewModel @Inject constructor(
    private val observePinUseCase: ObservePinUseCase,
    private val showPinUseCase: ShowPinUseCase,
    private val getQrUseCase: GetQrUseCase,
    private val saveAesKeyUseCase: SaveAesKeyUseCase
) : ViewModel() {

    private val TAG = "PairingVM"

    /** Estado principal de la UI */
    private val _uiState = MutableStateFlow(PairingState())
    val uiState: StateFlow<PairingState> = _uiState

    /**
     * Payload del QR generado por `/show_qr`.
     * La UI lo observa para construir el QR.
     */
    private val _qrPayload = MutableStateFlow<QrPayload?>(null)
    val qrPayload: StateFlow<QrPayload?> = _qrPayload

    init {
        /**
         * Observa el PIN emitido por el servidor.
         *
         * Flujo:
         * 1. El servidor ejecuta `/show_pin`.
         * 2. El UseCase `ObservePinUseCase` escucha el SharedFlow del servidor.
         * 3. Cuando llega el PIN → se actualiza el estado de UI.
         */
        viewModelScope.launch {
            observePinUseCase().collect { pin ->
                Log.d(TAG, "PIN recibido desde observePinUseCase(): $pin")

                _uiState.update {
                    it.copy(
                        pin = pin,
                        showPinDialog = true,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * # startPairing()
     *
     * Inicia el proceso de emparejamiento llamando al endpoint `/show_pin`
     * a través del [ShowPinUseCase].
     *
     * ## Flujo interno
     * 1. Marca la UI como cargando.
     * 2. Ejecuta el use case.
     * 3. Si todo va bien, espera a que el PIN llegue por Flow.
     * 4. Si falla, actualiza el estado con el error.
     */
    fun startPairing() {
        viewModelScope.launch {
            Log.d(TAG, "START → Llamando a /show_pin desde ShowPinUseCase()")

            _uiState.update {
                it.copy(isLoading = true, error = null)
            }

            val result = showPinUseCase()

            if (result.isSuccess) {
                Log.d(TAG, "START → /show_pin ejecutado correctamente (esperando PIN por Flow)")
            } else {
                val error = result.exceptionOrNull()?.message
                Log.e(TAG, "START → Error en /show_pin: $error")

                _uiState.update {
                    it.copy(isLoading = false, error = error)
                }
            }
        }
    }

    /**
     * # generateQr()
     *
     * Genera el PIN y el SALT en el servidor mediante [GetQrUseCase] y construye
     * el objeto [QrPayload] que la UI convertirá en un QR.
     *
     * ## Flujo interno
     * 1. Marca la UI como cargando.
     * 2. Ejecuta el use case.
     * 3. Si llega el payload → se expone en `_qrPayload`.
     * 4. La UI observa `qrPayload` y genera el QR.
     */
    fun generateQr() {
        viewModelScope.launch {
            Log.d(TAG, "QR → Solicitando QrPayload desde GetQrUseCase()")

            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = getQrUseCase()   // ← ahora sí, este es tu usecase real

            result.onSuccess { payload ->
                Log.d(TAG, "QR → Payload recibido: $payload")

                _qrPayload.value = payload

                _uiState.update { it.copy(isLoading = false) }
            }

            result.onFailure { error ->
                Log.e(TAG, "QR → Error generando QR: ${error.message}")

                _uiState.update {
                    it.copy(isLoading = false, error = error.message)
                }
            }
        }
    }

    /**
     * Cierra el diálogo del PIN y resetea el estado.
     *
     * Se usa cuando:
     * - El usuario cierra el diálogo manualmente.
     * - El emparejamiento se completa.
     * - Expira el tiempo de espera.
     */
    fun closePinDialog() {
        Log.d(TAG, "CLOSE → Cerrando diálogo del PIN y limpiando estado")

        _uiState.update {
            it.copy(
                showPinDialog = false,
                isPaired = false,
                pin = "",
                error = null
            )
        }

        Log.d(TAG, "CLOSE → Estado reseteado correctamente")
    }
}