package com.ajpr00.mobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ajpr00.core.domain.model.TabletConnectionData
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * QrScannerViewModel
 * ------------------
 * Este ViewModel es el "cerebro" que recibe el texto del QR detectado por ZXing
 * y lo transforma en un objeto TabletConnectionData.
 *
 * Piensa en él como el intermediario entre:
 *  - El lector de QR (CameraX + ZXing)
 *  - La pantalla que necesita los datos del dispositivo
 *
 * Además, evita que el QR se procese varias veces seguidas,
 * porque ZXing detecta el mismo QR muchas veces por segundo.
 */
class QrScannerViewModel @Inject constructor() : ViewModel() {

    // StateFlow que expone los datos ya parseados del QR.
    // La pantalla observa este flujo para saber cuándo cerrar el Dialog.
    private val _connectionData = MutableStateFlow<TabletConnectionData?>(null)
    val connectionData: StateFlow<TabletConnectionData?> = _connectionData.asStateFlow()

    // Flag para evitar procesar el mismo QR varias veces.
    private var processed = false

    /**
     * onQrDetected()
     * --------------
     * Este método lo llama el QRAnalyzer cada vez que detecta un QR.
     * Aquí decidimos si lo procesamos o lo ignoramos.
     */
    fun onQrDetected(raw: String) {
        Log.d("QR_VM", "📥 QR recibido en ViewModel: $raw")

        // Si ya procesamos un QR, ignoramos los siguientes.
        if (processed) {
            Log.d("QR_VM", "⛔ QR ignorado (ya procesado previamente)")
            return
        }

        processed = true
        Log.d("QR_VM", "🔄 Procesando QR por primera vez...")

        val parsed = parseQr(raw)
        Log.d("QR_VM", "📦 QR parseado correctamente: $parsed")

        _connectionData.value = parsed
    }

    /**
     * parseQr()
     * ---------
     * Convierte el texto del QR en un objeto TabletConnectionData.
     *
     * Formato esperado:
     *   id=xxx;token=xxx;ip=xxx;port=xxx
     *
     * Ejemplo real:
     *   id=tablet01;token=ABC123;ip=192.168.1.45;port=8080
     *
     * Si algún campo no existe, se rellena con valores por defecto.
     */
    private fun parseQr(raw: String): TabletConnectionData {
        Log.d("QR_VM", "🧩 Iniciando parseo del QR (Gson)...")

        val data = Gson().fromJson(raw, TabletConnectionData::class.java)

        Log.d("QR_VM", "🧪 Resultado del parseo → $data")

        return data
    }


    /**
     * reset()
     * -------
     * Resetea el estado del ViewModel para permitir leer otro QR.
     * Útil si el usuario vuelve a abrir el lector.
     */
    fun reset() {
        Log.d("QR_VM", "🔁 Reseteando estado del ViewModel")
        processed = false
        _connectionData.value = null
    }
}
