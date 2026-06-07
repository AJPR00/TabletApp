package com.ajpr00.core.security

import com.ajpr00.core.util.CoreLog
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * # Crypto
 *
 * Utilidad de cifrado basada en **AES‑GCM**, usada tanto en móvil como en tablet.
 *
 * ## ¿Por qué existe esta clase?
 * - Para encapsular toda la lógica de cifrado/descifrado.
 * - Para permitir dos formas de crear la clave AES:
 *   1. Desde un **String** (clave derivada).
 *   2. Desde un **ByteArray** (clave AES REAL).
 *
 * ## Casos de uso
 * - Durante la vinculación:
 *   - `Crypto(derivedKey)` para cifrar la clave AES_REAL.
 * - Durante el uso normal:
 *   - `Crypto(aesReal)` para cifrar/descifrar archivos.
 *
 * ## Advertencias
 * - AES‑GCM valida integridad: si la clave no coincide, el descifrado falla.
 * - Esta clase no genera claves; solo las usa.
 */
class Crypto {

    private val key: SecretKey

    companion object {
        private const val AES_KEY_SIZE = 16          // 128 bits
        private const val GCM_IV_SIZE = 12           // 96 bits
        private const val GCM_TAG_SIZE = 128         // bits
        private const val TAG = "Crypto"
    }

    /**
     * Constructor para claves derivadas desde texto plano.
     *
     * Ejemplo:
     * ```
     * Crypto("holaMundo")
     * ```
     *
     * Flujo:
     * - SHA‑256 al texto.
     * - Recorte a 16 bytes.
     */
    constructor(userPlainKey: String) {
        CoreLog.d(TAG, "Derivando clave AES desde String…")

        val keyBytes = MessageDigest.getInstance("SHA-256")
            .digest(userPlainKey.toByteArray())
            .copyOf(AES_KEY_SIZE)

        key = SecretKeySpec(keyBytes, "AES")

        CoreLog.d(TAG, "Clave AES derivada correctamente (${keyBytes.size} bytes)")
    }

    /**
     * Constructor para claves AES ya generadas (AES_REAL).
     *
     * Ejemplo:
     * ```
     * Crypto(aesRealByteArray)
     * ```
     *
     * Requisitos:
     * - Debe tener exactamente 16 bytes (AES‑128).
     */
    constructor(aesKeyBytes: ByteArray) {
        require(aesKeyBytes.size == AES_KEY_SIZE) {
            "AES key must be exactly 16 bytes for AES‑128"
        }

        CoreLog.d(TAG, "Usando clave AES REAL (${aesKeyBytes.size} bytes)")

        key = SecretKeySpec(aesKeyBytes, "AES")
    }

    /**
     * Cifra datos usando AES‑GCM.
     *
     * Flujo:
     * 1. Genera IV aleatorio de 12 bytes.
     * 2. Inicializa el cifrador.
     * 3. Cifra los datos.
     * 4. Devuelve `[IV] + [CIFRADO+TAG]`.
     */
    fun encrypt(plain: ByteArray): ByteArray {
        return try {
            CoreLog.d(TAG, "Iniciando cifrado…")

            val iv = ByteArray(GCM_IV_SIZE).apply {
                SecureRandom().nextBytes(this)
            }

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_SIZE, iv))

            val cipherText = cipher.doFinal(plain)

            CoreLog.d(TAG, "Cifrado completado (${cipherText.size} bytes)")

            iv + cipherText
        } catch (e: Exception) {
            CoreLog.e(TAG, "Error cifrando: ${e.message}")
            ByteArray(0)
        }
    }

    /**
     * Descifra datos cifrados con AES‑GCM.
     *
     * Flujo:
     * 1. Extrae IV (primeros 12 bytes).
     * 2. Extrae cipherText.
     * 3. Descifra.
     */
    fun decrypt(encrypted: ByteArray): ByteArray {
        return try {
            if (encrypted.size <= GCM_IV_SIZE) {
                CoreLog.e(TAG, "Datos demasiado cortos para descifrar")
                return ByteArray(0)
            }

            val iv = encrypted.copyOfRange(0, GCM_IV_SIZE)
            val cipherText = encrypted.copyOfRange(GCM_IV_SIZE, encrypted.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_SIZE, iv))

            cipher.doFinal(cipherText)
        } catch (e: AEADBadTagException) {
            CoreLog.e(TAG, "TAG inválido: clave incorrecta o datos corruptos")
            ByteArray(0)
        } catch (e: Exception) {
            CoreLog.e(TAG, "Error descifrando: ${e.message}")
            ByteArray(0)
        }
    }
}
