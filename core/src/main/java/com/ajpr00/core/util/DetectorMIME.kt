package com.ajpr00.core.util

fun detectorMIME(name: String): String {
    val lower = name.lowercase()

    return when {
        lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
        lower.endsWith(".png") -> "image/png"
        lower.endsWith(".webp") -> "image/webp"
        lower.endsWith(".mp4") -> "video/mp4"
        lower.endsWith(".mkv") -> "video/x-matroska"
        lower.endsWith(".mov") -> "video/quicktime"
        lower.endsWith(".avi") -> "video/x-msvideo"
        lower.endsWith(".gif") -> "image/gif"
        else -> "application/octet-stream"
    }
}
