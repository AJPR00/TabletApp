package com.ajpr00.tabletapp.util

import com.ajpr00.tabletapp.domain.model.FormatType

fun detectFormatType(path: String): FormatType {
    val lowerPath = path.lowercase()

    return when {
        lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg") || lowerPath.endsWith(".png") || lowerPath.endsWith(".gif") ->
            FormatType.IMAGE

        lowerPath.endsWith(".mp4") || lowerPath.endsWith(".mov") || lowerPath.endsWith(".mkv") || lowerPath.endsWith(".webm") ->
            FormatType.VIDEO

        lowerPath.endsWith(".mp3") || lowerPath.endsWith(".wav") || lowerPath.endsWith(".ogg") ->
            FormatType.AUDIO

        else -> throw Exception("Formato no reconocido $path")
    }
}