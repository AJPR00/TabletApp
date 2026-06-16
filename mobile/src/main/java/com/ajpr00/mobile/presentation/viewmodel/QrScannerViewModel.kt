package com.ajpr00.mobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.model.Eventos
import com.ajpr00.core.domain.model.qr.QrPayload
import com.ajpr00.mobile.qr.QrProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

@HiltViewModel
class QrScannerViewModel @Inject constructor(
    private val qrProcessor: QrProcessor
) : ViewModel() {

    private val _connectionData = MutableStateFlow<QrPayload?>(null)
    val connectionData: StateFlow<QrPayload?> = _connectionData.asStateFlow()

    private val _eventos = MutableSharedFlow<Eventos>()
    val eventos = _eventos

    private var processed = false

    fun onQrDetected(raw: String) {
        Log.d("QR_VM", "onQrDetected: QR recibido → $raw")

        //QR repetido → emitir evento
        if (processed) {
            Log.d("QR_VM", "onQrDetected: ignorado (ya procesado previamente)")
            viewModelScope.launch {
                _eventos.emit(Eventos.Info("QR ya procesado"))
            }
            return
        }

        processed = true
        Log.d("QR_VM", "onQrDetected: procesando QR por primera vez")

        val qrPayload = qrProcessor(raw)

        //QR inválido → emitir evento
        if (qrPayload == null) {
            viewModelScope.launch {
                _eventos.emit(Eventos.Error("QR inválido o no compatible"))
            }
            return
        }

        Log.d("QR_VM", "onQrDetected: dispositivo generado → $qrPayload")

        _connectionData.value = qrPayload

        //QR válido → emitir evento
        viewModelScope.launch {
            _eventos.emit(Eventos.Info("QR leído correctamente"))
        }
    }

    fun reset() {
        Log.d("QR_VM", "reset: limpiando estado del ViewModel")
        processed = false
        _connectionData.value = null
    }
}
