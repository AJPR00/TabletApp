package com.ajpr00.presentation_common.connectivity

import android.util.Log
import com.ajpr00.core.domain.usecase.network.CheckInternetConnectionUseCase

/**
 * Holder estático para almacenar una instancia de [CheckInternetConnectionUseCase].
 *
 * Este objeto existe porque algunas clases de la capa **presentation_common**
 * (como [NetworkObserver](ca://s?q=Documentar_NetworkObserver)) necesitan acceder
 * al caso de uso de conectividad, pero **no pueden recibirlo por inyección directa**
 * debido a limitaciones del framework Android:
 *
 * - Un `BroadcastReceiver` no puede recibir dependencias por constructor.
 * - Un `NetworkCallback` tampoco puede recibir dependencias por constructor.
 * - No se puede usar Hilt en clases creadas por el sistema.
 *
 * Por eso se utiliza este holder como “puente” entre Hilt y las clases del sistema.
 *
 * ## Cómo funciona
 * 1. Hilt inicializa el caso de uso en un módulo o Activity.
 * 2. Se asigna la instancia a `useCase`.
 * 3. Cualquier clase del sistema puede llamar a:
 *
 * ```
 * CheckInternetConnectionUseCaseHolder.useCase()
 * ```
 *
 * ## Advertencias importantes
 * - La propiedad `useCase` es `lateinit`: si no se inicializa antes de usarse,
 *   lanzará una excepción.
 * - Debe inicializarse **una sola vez** al arrancar la app.
 * - No debe usarse como sustituto de la inyección de dependencias normal.
 *
 * ## Excepciones
 *
 * ## Relación con la arquitectura
 * - **presentation_common** → usa este holder para acceder al caso de uso.
 * - **domain** → implementa la lógica real de comprobación.
 * - **data** → implementa la comprobación real de Internet (OkHttp/HTTP204).
 */
object CheckInternetConnectionUseCaseHolder {

    /**
     * Instancia del caso de uso de conectividad.
     *
     * Debe ser asignada por Hilt en el arranque de la aplicación.
     *
     */
    lateinit var useCase: CheckInternetConnectionUseCase
        private set

    /**
     * Asigna la instancia del caso de uso.
     *
     * Este método debe llamarse desde una clase con acceso a Hilt,
     * normalmente un `@AndroidEntryPoint` como MainActivity o un módulo.
     */
    fun initialize(instance: CheckInternetConnectionUseCase) {
        Log.d("CheckInternetHolder", "Inicializando CheckInternetConnectionUseCaseHolder")
        useCase = instance
    }

    /**
     * Ejecuta el caso de uso almacenado.
     *
     * @return `true` si hay conexión real a Internet, `false` si no.
     */
    suspend operator fun invoke(): Boolean {
        if (!::useCase.isInitialized) {
            Log.e("CheckInternetHolder", "useCase no inicializado antes de usarse")
        }

        Log.d("CheckInternetHolder", "Ejecutando CheckInternetConnectionUseCase desde el holder")
        return useCase()
    }
}
