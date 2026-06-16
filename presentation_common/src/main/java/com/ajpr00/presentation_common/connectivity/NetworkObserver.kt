package com.ajpr00.presentation_common.connectivity

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.util.Log
import kotlinx.coroutines.*

/**
 * Observador de conectividad para dispositivos con **Android 23 (Marshmallow)**,
 * donde `CONNECTIVITY_CHANGE` sigue siendo un mecanismo válido para detectar
 * cambios de red.
 *
 * Esta clase pertenece a la capa **presentation_common**, ya que:
 * - Depende directamente del framework Android (BroadcastReceiver).
 * - No implementa lógica de dominio ni comprobación real de Internet.
 * - Solo notifica cambios y delega la comprobación al caso de uso.
 *
 * ## Qué hace
 * Detecta cambios de red mediante un `BroadcastReceiver` y, cada vez que ocurre
 * un cambio, ejecuta una comprobación real de Internet usando:
 *
 * ```
 * CheckInternetConnectionUseCaseHolder.useCase()
 * ```
 *
 * Finalmente, notifica el resultado a la capa superior mediante `onStatusChanged`.
 *
 * ## Flujo interno
 * 1. Se registra un `ConnectivityReceiver` que escucha `CONNECTIVITY_CHANGE`.
 * 2. Cuando el sistema detecta un cambio, se ejecuta `onNetworkChanged`.
 * 3. `NetworkObserver` lanza una corrutina en IO.
 * 4. Se ejecuta el caso de uso de comprobación real de Internet.
 * 5. El resultado se devuelve al hilo principal.
 * 6. Se ejecuta `onStatusChanged(online)`.
 *
 * ## Advertencias importantes
 * - Este mecanismo **solo es válido para Android 23**.
 *   En Android 24+ debe usarse `NetworkCallback`.
 * - No detecta Internet real por sí mismo; solo detecta cambios de red.
 * - No debe contener lógica de negocio.
 * - No debe usarse en ViewModels (depende de Android).
 *
 * ## Relación con otras capas
 * - **presentation_common**: detecta cambios y notifica.
 * - **domain**: ejecuta `CheckInternetConnectionUseCase`.
 * - **data**: implementa la comprobación real de Internet (OkHttp).
 */
class NetworkObserver(
    private val context: Context,
    private val onStatusChanged: (Boolean) -> Unit
) {

    private val receiver = ConnectivityReceiver {
        Log.d("NetworkObserver", "Cambio de red detectado. Ejecutando comprobación real.")
        checkInternet()
    }

    private val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)

    /**
     * Registra el `BroadcastReceiver` y realiza una comprobación inicial.
     *
     * Debe llamarse normalmente en `onStart()` o `onResume()` del Activity.
     */
    fun register() {
        Log.d("NetworkObserver", "Registrando ConnectivityReceiver para Android 23")
        context.registerReceiver(receiver, filter)
        checkInternet()
    }

    /**
     * Desregistra el `BroadcastReceiver`.
     *
     * Debe llamarse en `onStop()` o `onDestroy()` del Activity.
     */
    fun unregister() {
        Log.d("NetworkObserver", "Desregistrando ConnectivityReceiver")
        context.unregisterReceiver(receiver)
    }

    /**
     * Comprueba si hay conexión real a Internet.
     *
     * Esta función:
     * - Se ejecuta en un hilo de IO.
     * - Llama al caso de uso real de conectividad.
     * - Devuelve el resultado al hilo principal.
     *
     */
    private fun checkInternet() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("NetworkObserver", "Ejecutando CheckInternetConnectionUseCase en IO")
                val online = CheckInternetConnectionUseCaseHolder.useCase()

                withContext(Dispatchers.Main) {
                    Log.d("NetworkObserver", "Resultado de conectividad: $online")
                    onStatusChanged(online)
                }

            } catch (e: Exception) {
                Log.e("NetworkObserver", "Error comprobando Internet: ${e.message}")
            }
        }
    }
}