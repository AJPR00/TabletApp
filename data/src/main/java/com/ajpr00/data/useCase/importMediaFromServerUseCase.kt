package com.ajpr00.data.useCase

import android.content.Context
import com.ajpr00.core.domain.model.FormatType
import com.ajpr00.core.domain.repository.MediaRepository
import com.ajpr00.data.file.FileManager.saveIncomingFile
import com.ajpr00.data.mapper.toMediaContent
import com.ajpr00.data.util.detectFormatType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class ImportMediaFromServerUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: MediaRepository
) {
    suspend operator fun invoke(tempFile: File, mime: String, extension: String) {

        val type = when (extension.lowercase()) {
            "jpg", "jpeg", "png", "webp" -> FormatType.IMAGE
            "mp4", "mov", "mkv" -> FormatType.VIDEO
            else -> detectFormatType(context, uri = null, mime = mime)
        }

        val finalFile = saveIncomingFile(context, tempFile, mime, type, extension)

        val media = finalFile.toMediaContent(type)
        repo.addBd(media)
    }
}
