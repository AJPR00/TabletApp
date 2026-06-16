package com.ajpr00.core.security

import com.ajpr00.core.domain.excepcion.FileCoreException
import com.ajpr00.core.util.CoreLog
import java.io.File
import java.io.IOException
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * ## CryptoStreamDecrypt
 *
 * Utilidad para **descifrar archivos grandes** cifrados con AES‑GCM por streaming.
 *
 * ### Responsabilidad dentro de la arquitectura
 * - Pertenece al módulo **core**, sin dependencias de Android.
 * - Descifra archivos sin cargarlos completos en memoria.
 * - Maneja archivos grandes (vídeos, backups, multimedia pesada).
 *
 * ### Formato esperado del archivo cifrado
 * ```
 * [IV][CIPHERTEXT + TAG]
 * ```
 * - IV → 12 bytes iniciales.
 * - TAG → 16 bytes finales generados por AES‑GCM.
 *
 * ### Flujo interno del descifrado
 * 1. Lee los primeros 12 bytes (IV).
 * 2. Inicializa AES‑GCM con la clave y el IV.
 * 3. Lee el resto del archivo por bloques de 64 KB.
 * 4. Descifra cada bloque con `cipher.update()`.
 * 5. Descifra los bytes finales con `cipher.doFinal()`.
 *
 * ### Excepciones
 * - `FileCoreException.ReadError` si:
 *   - El archivo es demasiado pequeño.
 *   - El IV no puede leerse.
 *   - El TAG es inválido (clave incorrecta o datos corruptos).
 *   - Ocurre un error de E/S.
 */
class CryptoStreamDecrypt(aesKey: ByteArray) {

    companion object {
        private const val GCM_IV_SIZE = 12
        private const val GCM_TAG_SIZE = 128
        private const val BUFFER_SIZE = 64 * 1024
        private const val TAG = "CryptoStreamDecrypt"
    }

    private val key = SecretKeySpec(aesKey, "AES")

    /**
     * Descifra un archivo grande cifrado con AES‑GCM por streaming.
     *
     * @param input Archivo cifrado.
     * @param output Archivo destino descifrado.
     *
     * @throws FileCoreException.ReadError si ocurre cualquier error de descifrado o lectura.
     */
    fun decryptFile(input: File, output: File) {
        try {
            CoreLog.d(TAG, "Descifrado streaming iniciado…")
            CoreLog.d(TAG, "Archivo origen: ${input.absolutePath} (${input.length()} bytes)")

            if (!input.exists() || input.length() <= GCM_IV_SIZE) {
                CoreLog.e(TAG, "Archivo demasiado pequeño o inexistente")
                throw FileCoreException.ReadError
            }

            val iv = ByteArray(GCM_IV_SIZE)

            input.inputStream().use { inputStream ->

                // Leer IV
                val ivRead = inputStream.read(iv)
                if (ivRead != GCM_IV_SIZE) {
                    CoreLog.e(TAG, "No se pudo leer el IV completo")
                    throw FileCoreException.ReadError
                }

                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_SIZE, iv))

                output.outputStream().use { out ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int

                    while (true) {
                        bytesRead = inputStream.read(buffer)
                        if (bytesRead == -1) break

                        val decryptedChunk = cipher.update(buffer, 0, bytesRead)
                        if (decryptedChunk != null) {
                            out.write(decryptedChunk)
                        }
                    }

                    // TAG final + últimos bytes
                    val finalBytes = cipher.doFinal()
                    out.write(finalBytes)
                }
            }

            CoreLog.d(TAG, "Descifrado streaming completado: ${output.length()} bytes")

        } catch (e: AEADBadTagException) {
            CoreLog.e(TAG, "TAG inválido: clave incorrecta o datos corruptos")
            throw FileCoreException.ReadError

        } catch (e: IOException) {
            CoreLog.e(TAG, "Error de E/S descifrando archivo: ${e.message}")
            throw FileCoreException.ReadError

        } catch (e: Exception) {
            CoreLog.e(TAG, "Error descifrando archivo grande: ${e.message}")
            throw FileCoreException.ReadError
        }
    }
}
