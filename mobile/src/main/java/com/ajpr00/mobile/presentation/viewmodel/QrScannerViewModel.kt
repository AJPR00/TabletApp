package com.ajpr00.mobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.mobile.qr.QrProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel encargado de recibir el texto del QR detectado y exponer
 * el resultado ya procesado a la UI.
 *
 * Arquitectura:
 * - Capa: presentation
 * - Rol: coordinar el flujo QR → dominio → UI.
 * - No parsea JSON ni construye modelos; delega todo al UseCase.
 *
 * Flujo:
 * 1. Recibe el texto del QR desde CameraX/ZXing.
 * 2. Evita procesar el mismo QR varias veces.
 * 3. Llama al UseCase para convertir el raw en un `Dispositivo`.
 * 4. Expone el resultado por StateFlow para que la UI reaccione.
 */
class QrScannerViewModel @Inject constructor(
    private val qrProcessor: QrProcessor
) : ViewModel() {

    private val _connectionData = MutableStateFlow<Dispositivo?>(null)
    val connectionData: StateFlow<Dispositivo?> = _connectionData.asStateFlow()

    private var processed = false

    /**
     * Recibe el texto del QR detectado y delega su procesamiento al UseCase.
     */
    fun onQrDetected(raw: String) {
        Log.d("QR_VM", "onQrDetected: QR recibido → $raw")

        if (processed) {
            Log.d("QR_VM", "onQrDetected: ignorado (ya procesado previamente)")
            return
        }

        processed = true
        Log.d("QR_VM", "onQrDetected: procesando QR por primera vez")

        val dispositivo = qrProcessor(raw)

        Log.d("QR_VM", "onQrDetected: dispositivo generado → $dispositivo")

        _connectionData.value = dispositivo
    }

    /**
     * Permite volver a escanear otro QR.
     */
    fun reset() {
        Log.d("QR_VM", "reset: limpiando estado del ViewModel")
        processed = false
        _connectionData.value = null
    }
}
