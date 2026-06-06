package com.ajpr00.core.domain.usecase.media

import com.ajpr00.core.domain.model.PendingMedia
import com.ajpr00.core.domain.repository.media.TabletApiRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class SendEncryptedMediaUseCase @Inject constructor(
    private val apiRepo: TabletApiRepository
) {

    suspend operator fun invoke(
        media: PendingMedia,
        ip: String,
        port: Int,
        userKey: String
    ): Boolean {
        val file = File(media.filePath)
        if (!file.exists()) return false

        // 1. Leer bytes
        val bytes = file.readBytes()

        // 2. Cifrar con AES‑GCM
        val crypto = Crypto(userKey)
        val encrypted = crypto.encrypt(bytes)
        if (encrypted.isEmpty()) return false

        // 3. Preparar multipart con datos cifrados
        val requestBody = encrypted.toRequestBody("application/octet-stream".toMediaType())
        val part = MultipartBody.Part.createFormData(
            "file",
            file.name,
            requestBody
        )

        // 4. Enviar al servidor tablet
        val response = apiRepo.uploadMedia(ip, port, part) ?: return false
        return response.status == "OK" && (response.data?.success == true)
    }
}
