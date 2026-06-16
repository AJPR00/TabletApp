package com.ajpr00.presentation_common.connectivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ajpr00.data.exception.NetworkException

/**
 * BroadcastReceiver encargado de detectar cambios de conectividad en dispositivos
 * con **Android 23 (Marshmallow)** o versiones donde `CONNECTIVITY_CHANGE` sigue siendo válido.
 *
 * Esta clase pertenece a la capa **presentation_common**, ya que:
 * - Es infraestructura dependiente del framework Android.
 * - No contiene lógica de dominio ni lógica de red real.
 * - Solo notifica a la capa superior cuando el sistema informa un cambio.
 *
 * ## Cuándo se usa este receiver
 * - En Android 23 → es la única forma fiable de detectar cambios de red.
 * - En Android 24+ → este mecanismo está deprecado y no funciona para apps normales.
 *
 * Por eso, en una arquitectura moderna se combina con:
 * - `NetworkCallback` para Android 24+
 * - Este `BroadcastReceiver` para Android 23
 *
 * ## Flujo interno
 * 1. El sistema emite un broadcast cuando cambia la red.
 * 2. Android invoca `onReceive`.
 * 3. Se ejecuta el callback `onNetworkChanged`.
 * 4. La capa superior decide si quiere comprobar Internet real (OkHttp, ping, etc.).
 *
 * ## Advertencias importantes
 * - No detecta si la red tiene Internet real, solo que hubo un cambio.
 * - No debe usarse en Android 24+ como único mecanismo.
 * - No debe contener lógica de negocio.
 *
 * ## Excepciones
 * Puede lanzar:
 * - [NetworkException] si ocurre un error inesperado al procesar el broadcast.
 *
 * ## Relación con otras capas
 * - **presentation_common**: recibe el evento del sistema.
 * - **domain**: ejecuta casos de uso como `CheckInternetConnectionUseCase`.
 * - **data**: implementa la comprobación real de Internet.
 */
class ConnectivityReceiver(
    private val onNetworkChanged: () -> Unit
) : BroadcastReceiver() {

    /**
     * Método invocado automáticamente por Android cuando ocurre un cambio de red.
     *
     * @param context Contexto del sistema que emite el broadcast.
     * @param intent Intent que describe el evento de cambio de conectividad.
     *
     * @throws NetworkException Si ocurre un error inesperado al ejecutar el callback.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("ConnectivityReceiver", "Broadcast de conectividad recibido")

        try {
            onNetworkChanged()
        } catch (e: Exception) {
            Log.e("ConnectivityReceiver", "Error procesando cambio de red: ${e.message}")
            // NO lanzar excepciones aquí
        }
    }
}