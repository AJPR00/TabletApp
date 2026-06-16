package com.ajpr00.core.domain.usecase.media

import com.ajpr00.core.security.CryptoStream
import com.ajpr00.core.domain.model.PendingMedia
import com.ajpr00.core.domain.repository.media.TabletApiRepository
import com.ajpr00.core.domain.excepcion.MediaException
import com.ajpr00.core.util.CoreLog
import com.ajpr00.core.util.detectorMIME
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

class SendEncryptedMediaUseCase @Inject constructor(
    private val apiRepo: TabletApiRepository
) {
    val TAG ="SendEncryptedMedia"

    suspend operator fun invoke(
        media: PendingMedia,
        ip: String,
        port: Int,
        aesKey: ByteArray
    ) {
        CoreLog.d(TAG, "---- INICIO ENVÍO CIFRADO ----")
        CoreLog.d(TAG, "Archivo: ${media.filePath}")
        CoreLog.d(TAG, "AES Key size: ${aesKey.size} bytes")
        CoreLog.d(TAG, "Destino: $ip:$port")

        val file = File(media.filePath)

        if (!file.exists()) {
            CoreLog.e(TAG, "ERROR: Archivo no encontrado")
            throw MediaException.FileNotFound
        }
        val originalName = file.name
        val originalExt = file.extension.lowercase()
        val originalMime = detectorMIME(originalName)

        CoreLog.d(TAG, "Tamaño archivo: ${file.length()} bytes")

        // -----------------------------
        // 1. CIFRADO POR STREAMING
        // -----------------------------
        CoreLog.d(TAG, "Cifrando archivo por streaming…")

        val encryptedFile = File(file.parent, file.name+"_vis")
        CoreLog.d(TAG, "Archivo cifrado: ${file.parent} y ${file.name}")

        val timeStart = System.currentTimeMillis()
        try {
            CryptoStream(aesKey).encryptFile(file, encryptedFile)
        } catch (e: Exception) {
            CoreLog.e(TAG, "ERROR cifrando archivo grande: ${e.message}")
            throw MediaException.EncryptError
        }
        val timeFin = System.currentTimeMillis()

        CoreLog.d(TAG,"Cifrado OK (${encryptedFile.name} bytes) en ${timeFin - timeStart} ms"
        )

        // -----------------------------
        // 2. SUBIDA DEL ARCHIVO CIFRADO
        // -----------------------------
        val requestBody = encryptedFile.asRequestBody("application/octet-stream".toMediaType())
        val part = MultipartBody.Part.createFormData("file", encryptedFile.name, requestBody)
        val namePart = originalName.toRequestBody(null)
        val extPart = originalExt.toRequestBody(null)
        val mimePart = originalMime.toRequestBody(null)

        val tUploadStart = System.currentTimeMillis()
        val response = try {
            apiRepo.uploadMedia(ip, port, part, namePart, extPart, mimePart)
        } catch (e: Exception) {
            CoreLog.e(TAG, "ERROR subiendo archivo: ${e.message}")
            throw MediaException.UploadError
        }
        val tUploadEnd = System.currentTimeMillis()

        CoreLog.d(TAG, "Upload completado en ${tUploadEnd - tUploadStart} ms")
        CoreLog.d(TAG,"Respuesta servidor: status=${response.status}, success=${response.data?.success}"
        )

        val ok = response.status == "OK" && (response.data?.success == true)

        if (!ok) {
            CoreLog.e(TAG, "ERROR: Servidor devolvió fallo en upload")
            throw MediaException.UploadError
        }

        CoreLog.d(TAG, "---- ENVÍO CIFRADO COMPLETADO ----")
    }

    private fun ByteArray.sha256(): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(this)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
