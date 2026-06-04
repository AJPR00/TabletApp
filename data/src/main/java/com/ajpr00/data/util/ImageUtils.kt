package com.ajpr00.data.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.scale

object ImageUtils {

    fun generateThumbnail(
        originalFile: File,
        maxSize: Int = 200,
        quality: Int = 80
    ): File {

        // 1. Decodificar la imagen original
        val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath)

        // 2. Calcular proporción
        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val width: Int
        val height: Int

        if (ratio > 1) {
            width = maxSize
            height = (maxSize / ratio).toInt()
        } else {
            height = maxSize
            width = (maxSize * ratio).toInt()
        }

        // 3. Escalar la imagen
        val scaled = bitmap.scale(width, height)

        // 4. Comprimir a JPEG
        val output = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, output)
        val bytes = output.toByteArray()

        // 5. Crear archivo thumbnail
        val thumbFile = File(
            originalFile.parent,
            originalFile.nameWithoutExtension + "_thumb.jpg"
        )

        FileOutputStream(thumbFile).use { it.write(bytes) }

        return thumbFile
    }
}

object VideoUtils {

    fun generateVideoThumbnail(
        videoFile: File,
        quality: Int = 80
    ): File {

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoFile.absolutePath)

        val bitmap: Bitmap = retriever.getFrameAtTime(0)
            ?: throw Exception("No se pudo extraer frame del vídeo")

        val thumbFile = File(
            videoFile.parent,
            videoFile.nameWithoutExtension + "_thumb.jpg"
        )

        FileOutputStream(thumbFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }

        retriever.release()

        return thumbFile
    }
}

