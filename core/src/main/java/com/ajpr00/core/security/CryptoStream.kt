package com.ajpr00.core.security

import com.ajpr00.core.domain.excepcion.FileCoreException
import com.ajpr00.core.util.CoreLog
import java.io.File
import java.io.IOException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * ## CryptoStream
 *
 * Utilidad de cifrado **por streaming** basada en AES‑GCM, diseñada para manejar
 * archivos grandes sin cargarlos completos en memoria.
 *
 * ### Responsabilidad dentro de la arquitectura
 * - Pertenece al módulo **core**, por lo que no depende de Android.
 * - Encapsula el cifrado de archivos grandes usando buffers.
 * - Evita OOM y truncados al procesar vídeos o archivos pesados.
 *
 * ### Cuándo usar esta clase
 * - Para cifrar archivos grandes antes de enviarlos por red.
 * - Para procesar vídeos, imágenes pesadas o backups.
 *
 * ### Flujo interno del cifrado
 * 1. Genera un IV aleatorio de 12 bytes.
 * 2. Inicializa AES‑GCM con la clave proporcionada.
 * 3. Lee el archivo por bloques de 64 KB.
 * 4. Cifra cada bloque con `cipher.update()`.
 * 5. Escribe el IV al inicio del archivo de salida.
 * 6. Escribe el TAG final con `cipher.doFinal()`.
 *
 * ### Formato de salida
 * ```
 * [IV][CIPHERTEXT + TAG]
 * ```
 *
 * ### Excepciones
 * - `FileCoreException.WriteError` si ocurre cualquier error de E/S o cifrado.
 */
class CryptoStream(aesKey: ByteArray) {

    companion object {
        private const val GCM_IV_SIZE = 12
        private const val GCM_TAG_SIZE = 128
        private const val BUFFER_SIZE = 64 * 1024 // 64 KB
        private const val TAG = "CryptoStream"
    }

    private val key = SecretKeySpec(aesKey, "AES")

    /**
     * Cifra un archivo grande por streaming usando AES‑GCM.
     *
     * ### Flujo interno
     * 1. Genera IV aleatorio.
     * 2. Inicializa el cifrador.
     * 3. Escribe el IV al inicio del archivo destino.
     * 4. Lee el archivo origen por bloques.
     * 5. Cifra cada bloque y lo escribe.
     * 6. Escribe los bytes finales (TAG).
     *
     * @param input Archivo de entrada sin cifrar.
     * @param output Archivo de salida cifrado.
     *
     * @throws FileCoreException.WriteError si ocurre un error de lectura, escritura o cifrado.
     */
    fun encryptFile(input: File, output: File) {
        try {
            CoreLog.d(TAG, "Iniciando cifrado por streaming…")
            CoreLog.d(TAG, "Archivo origen: ${input.absolutePath} (${input.length()} bytes)")
            CoreLog.d(TAG, "Archivo destino: ${output.absolutePath}")

            if (!input.exists() || !input.canRead()) {
                CoreLog.e(TAG, "Archivo de entrada no accesible")
                throw FileCoreException.ReadError
            }

            val iv = ByteArray(GCM_IV_SIZE).apply {
                SecureRandom().nextBytes(this)
            }

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_SIZE, iv))

            output.outputStream().use { out ->
                input.inputStream().use { inputStream ->

                    // Escribir IV al inicio
                    out.write(iv)

                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int

                    while (true) {
                        bytesRead = inputStream.read(buffer)
                        if (bytesRead == -1) break

                        val encryptedChunk = cipher.update(buffer, 0, bytesRead)
                        if (encryptedChunk != null) {
                            out.write(encryptedChunk)
                        }
                    }

                    // TAG final + últimos bytes
                    val finalBytes = cipher.doFinal()
                    out.write(finalBytes)
                }
            }

            CoreLog.d(TAG, "Cifrado completado correctamente")
            CoreLog.d(TAG, "Tamaño final del archivo cifrado: ${output.length()} bytes")

        } catch (e: IOException) {
            CoreLog.e(TAG, "Error de E/S cifrando archivo: ${e.message}")
            throw FileCoreException.WriteError

        } catch (e: Exception) {
            CoreLog.e(TAG, "Error cifrando archivo grande: ${e.message}")
            throw FileCoreException.WriteError
        }
    }
}