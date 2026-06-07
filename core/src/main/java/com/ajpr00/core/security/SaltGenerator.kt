package com.ajpr00.core.security

import com.ajpr00.core.util.CoreLog
import java.security.SecureRandom

/**
 * # SaltGenerator
 *
 * Utilidad para generar un **salt criptográfico seguro** para PBKDF2.
 * El salt se usa durante el emparejamiento LAN para derivar una clave
 * temporal desde el PIN de 6 dígitos.
 *
 * ## ¿Por qué es necesario un salt?
 * - Evita ataques de diccionario.
 * - Evita que dos tablets con el mismo PIN generen la misma clave temporal.
 * - Aumenta la entropía del proceso PBKDF2.
 *
 * ## Características
 * - Tamaño recomendado: **16 bytes** (128 bits).
 * - Generado con `SecureRandom` (seguro).
 * - Se envía al móvil junto con el paquete cifrado.
 *
 * ## Relación con otras capas
 * - **tablet/server** → genera el salt y lo envía en `/pair`.
 * - **mobile/data** → usa el salt para derivar la misma clave temporal.
 */
object SaltGenerator {

    private const val TAG = "SaltGenerator"

    /**
     * Genera un salt seguro de 16 bytes.
     *
     * ## Flujo interno
     * 1. Crea un array de 16 bytes.
     * 2. Lo rellena con valores aleatorios seguros.
     * 3. Lo devuelve tal cual.
     *
     * @return Array de 16 bytes aleatorios.
     */
    fun generateSalt16(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        CoreLog.d(TAG, "Salt generado (${salt.size} bytes)")

        return salt
    }
}
