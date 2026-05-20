package com.ajpr00.data.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ajpr00.core.domain.model.FormatType

/**
 * Detecta el tipo de formato de un archivo.
 *
 * - Si viene de Drive/FTP: se pasa MIME directo.
 * - Si viene del dispositivo: se usa Context + Uri.
 * - Si no se puede detectar: devuelve UNKNOWN.
 */
fun detectFormatType(
    context: Context? = null,
    uri: Uri? = null,
    mime: String? = null
): FormatType {

    // 1. MIME directo (Drive, Dropbox, FTP)
    val resolvedMime = mime ?: if (context != null && uri != null) context.contentResolver.getType(uri) else null

    if (resolvedMime == null) {
        Log.w("MimeType", "No se pudo detectar MIME, devolviendo UNKNOWN")
        return FormatType.UNKNOWN
    }

    Log.d("MimeType", "Clasificando MIME: $resolvedMime")

    // 2. Clasificación
    return when {
        resolvedMime.startsWith("image") -> FormatType.IMAGE
        resolvedMime.startsWith("video") -> FormatType.VIDEO
        else -> FormatType.UNKNOWN
    }
}
