package com.ajpr00.core.security

import com.ajpr00.core.util.CoreLog
import java.security.SecureRandom

/**
 * # PinGenerator
 *
 * Generar un **PIN que se usa únicamente durante el **emparejamiento LAN** entre
 * móvil ↔ tablet.
 *
 * ## ¿Para qué sirve este PIN?
 * - Solo sirve para derivar una **clave temporal** mediante PBKDF2.
 * - Esa clave temporal se usa para cifrar la **clave AES REAL**.
 *
 * ## Características
 * - Seguro: usa `SecureRandom`.
 *
 * ## Relación con otras capas
 * - **tablet/presentation** → muestra el PIN en pantalla.
 * - **mobile/presentation** → el usuario lo introduce.
 * - **tablet/server** → valida el PIN y deriva la clave temporal.
 * - **mobile/data** → deriva la misma clave temporal para descifrar la clave AES real.
 */
object PinGenerator {

    private const val TAG = "PinGenerator"

    /**
     * Genera un PIN seguro de 6 dígitos.
     *
     * ## Flujo interno
     * 1. Se genera un número aleatorio entre 0 y 999999.
     * 2. Se formatea con ceros a la izquierda.
     * 3. Se devuelve como `String`.
     *
     * @return PIN de 6 dígitos, siempre en formato `"000000"`.
     */
    @Suppress("DefaultLocale")
    fun generatePin6(): String {
        val random = SecureRandom()
        val number = random.nextInt(1_000_000) // 0 → 999999

        val pin = String.format("%06d", number)

        CoreLog.d(TAG, "PIN generado: $pin")

        return pin
    }
}
