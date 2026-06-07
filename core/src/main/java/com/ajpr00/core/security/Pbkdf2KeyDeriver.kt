package com.ajpr00.core.security

import com.ajpr00.core.util.CoreLog
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * # Pbkdf2KeyDeriver
 *
 * Utilidad para **derivar una clave AES temporal** a partir de:
 *
 * - Un PIN de 6 dígitos (ej: `"483921"`).
 * - Un `salt` aleatorio (16 bytes).
 *
 * Esta clave derivada se usa **solo durante el emparejamiento LAN** para
 * cifrar/descifrar la **clave AES REAL** que luego usará `Crypto`.
 *
 * ## ¿Por qué PBKDF2?
 * - El PIN es débil (6 dígitos).
 * - PBKDF2 lo “endurece” aplicando muchas iteraciones.
 * - El salt evita que dos dispositivos con el mismo PIN generen la misma clave.
 *
 * ## Relación con otras capas
 * - **tablet/server** → deriva la clave temporal para cifrar AES_REAL.
 * - **mobile/data** → deriva la misma clave temporal para descifrar AES_REAL.
 */
object Pbkdf2KeyDeriver {

    private const val TAG = "Pbkdf2KeyDeriver"

    // Parámetros recomendados para PBKDF2
    private const val ITERATIONS = 10_000
    private const val KEY_LENGTH_BITS = 128 // 16 bytes → AES‑128

    /**
     * Deriva una clave AES‑128 desde un PIN y un salt usando PBKDF2.
     *
     * ## Flujo interno
     * 1. Crea un `PBEKeySpec` con:
     *    - PIN como contraseña.
     *    - salt como bytes.
     *    - número de iteraciones.
     *    - longitud de clave en bits.
     * 2. Usa `PBKDF2WithHmacSHA256` para generar la clave.
     * 3. Devuelve los bytes de la clave derivada.
     *
     * ## Advertencias
     * - El mismo `pin + salt` debe usarse en tablet y móvil para obtener
     *   la misma clave.
     * - Cambiar iteraciones o longitud rompe compatibilidad.
     *
     * @param pin PIN de emparejamiento (ej: `"483921"`).
     * @param salt Salt aleatorio de 16 bytes generado por la tablet.
     * @return Clave derivada de 16 bytes lista para usar como AES‑128.
     */
    fun deriveKeyFromPin(pin: String, salt: ByteArray): ByteArray {
        return try {
            CoreLog.d(TAG, "Derivando clave desde PIN + salt…")

            val keySpec = PBEKeySpec(
                pin.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH_BITS
            )

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val keyBytes = factory.generateSecret(keySpec).encoded

            CoreLog.d(TAG, "Clave derivada correctamente (${keyBytes.size} bytes)")

            keyBytes
        } catch (e: Exception) {
            CoreLog.e(TAG, "Error derivando clave PBKDF2: ${e.message}")
            ByteArray(0)
        }
    }
}