package com.ajpr00.visumloop.tablet.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ajpr00.visumloop.tablet.domain.model.FormatType

// Detecta el tipo de formato de un archivo dado su ruta
fun detectFormatType(context: Context, uri: Uri): FormatType {
    val mime = context.contentResolver.getType(uri) ?: return FormatType.IMAGE
    Log.d("MimeType", "El MIME es: $mime")
    return when {
        mime.startsWith("image") -> FormatType.IMAGE
        mime.startsWith("video") -> FormatType.VIDEO
        mime.startsWith("audio") -> FormatType.AUDIO
        else -> throw Exception("Formato no reconocido $mime")
    }
}