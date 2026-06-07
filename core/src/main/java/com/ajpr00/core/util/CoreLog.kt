package com.ajpr00.core.util

/**
 * # CoreLog
 *
 * Logger minimalista pensado para módulos **puros** como `core`, donde
 * **no existe android.util.Log**.
 *
 * - Por defecto no hace nada (modo silencioso).
 * - Desde Android (mobile/tablet) puedes asignar lambdas para redirigir
 *   los logs a Logcat.
 *
 * ## Ejemplo de activación en Android
 * ```
 * CoreLog.debug = { tag, msg -> Log.d(tag, msg) }
 * CoreLog.error = { tag, msg -> Log.e(tag, msg) }
 * ```
 *
 * ## Objetivo
 * Permitir que `core` tenga logs sin romper la arquitectura ni añadir
 * dependencias de Android.
 */
object CoreLog {

    /** Función opcional para logs de debug. */
    var debug: ((tag: String, msg: String) -> Unit)? = null

    /** Función opcional para logs de error. */
    var error: ((tag: String, msg: String) -> Unit)? = null

    /** Log de debug seguro. */
    fun d(tag: String, msg: String) {
        debug?.invoke(tag, msg)
    }

    /** Log de error seguro. */
    fun e(tag: String, msg: String) {
        error?.invoke(tag, msg)
    }
}
