package com.ajpr00.tablet.domain.usecase

import com.ajpr00.core.domain.repository.preference.PreferencesRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

/**
 * # InitTabletUseCase
 *
 * Caso de uso encargado de inicializar la **identidad de la tablet** durante el
 * primer arranque de la aplicación.
 *
 * ## ¿Qué hace exactamente?
 * - Comprueba si es la primera vez que se ejecuta la app.
 * - Si es así:
 *   - Genera un `tabletId` único mediante `UUID.randomUUID()`.
 *   - Guarda el nombre elegido por el usuario.
 *   - Marca el onboarding como completado.
 *
 * ## Flujo interno
 * 1. Lee `isFirstRun()` desde `PreferencesRepository`.
 * 2. Si es `true`, genera un UUID.
 * 3. Guarda `tabletId` y `tabletName`.
 * 4. Marca `firstRunCompleted`.
 *
 * ## Relación con otras capas
 * - **presentation/tablet**: la pantalla de onboarding llama a este caso de uso.
 * - **data/preferences**: escribe los valores persistentes.
 * - **server/mDNS**: usa `tabletId` y `tabletName` para anunciar el servicio.
 *
 * @param prefs Repositorio de preferencias donde se guardan los datos persistentes.
 */
class InitTabletUseCase @Inject constructor(
    private val prefs: PreferencesRepository
) {

    /**
     * Ejecuta la inicialización de la tablet.
     *
     * @param name Nombre que el usuario asigna a la tablet (ej: "Marco del salón").
     */
    suspend operator fun invoke(name: String) {
        // Log pedagógico para entender el flujo
        println("InitTabletUseCase → Comprobando si es el primer arranque...")

        val firstRun = prefs.isFirstRun().first()

        if (firstRun) {
            println("InitTabletUseCase → Primer arranque detectado. Generando identidad...")

            // 1. Generar ID único
            val uuid = UUID.randomUUID().toString()

            // 2. Guardar identidad
            prefs.setTabletId(uuid)
            prefs.setTabletName(name)

            // 3. Marcar onboarding completado
            prefs.setFirstRunCompleted()

            println("InitTabletUseCase → Identidad creada correctamente. ID=$uuid, name=$name")
        } else {
            println("InitTabletUseCase → No es el primer arranque. No se modifica la identidad.")
        }
    }
}
