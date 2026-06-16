package com.ajpr00.data.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ajpr00.core.domain.model.FormatType
import com.ajpr00.core.domain.excepcion.MediaException

/**
 * ## detectFormatType
 *
 * Función utilitaria para detectar el tipo de archivo (imagen o vídeo)
 * a partir de:
 *
 * - Un MIME directo (Drive, Dropbox, FTP).
 * - Un `Uri` local del dispositivo (requiere `Context`).
 *
 * ### Responsabilidad dentro de la arquitectura
 * - Pertenece a la **Data layer**.
 * - No accede a red ni a BD.
 * - Solo interpreta MIME y devuelve un `FormatType`.
 *
 * ### Flujo interno
 * 1. Si se pasa `mime`, se usa directamente.
 * 2. Si no, y se pasa `context + uri`, se consulta el MIME al sistema.
 * 3. Si no se puede obtener MIME → lanza `MediaException.ReadError`.
 * 4. Clasifica según prefijo del MIME.
 *
 * ### Parámetros
 * @param context Context necesario para resolver MIME desde un `Uri`.
 * @param uri `Uri` del archivo local.
 * @param mime MIME directo si viene de la nube.
 *
 * ### Valor de retorno
 * Un `FormatType` indicando si es imagen, vídeo o desconocido.
 *
 * ### Excepciones
 * - `MediaException.ReadError` si no se puede obtener el MIME.
 */
fun detectFormatType(
    context: Context? = null,
    uri: Uri? = null,
    mime: String? = null
): FormatType {

    // 1. MIME directo (Drive, Dropbox, FTP)
    val resolvedMime = mime ?: run {
        if (context != null && uri != null) {
            try {
                context.contentResolver.getType(uri)
            } catch (e: Exception) {
                Log.e("MimeType", "Error obteniendo MIME desde Uri: ${e.message}")
                throw MediaException.ReadError
            }
        } else null
    }

    if (resolvedMime == null) {
        Log.w("MimeType", "No se pudo detectar MIME → MediaException.ReadError")
        throw MediaException.ReadError
    }

    Log.d("MimeType", "Clasificando MIME: $resolvedMime")

    // 2. Clasificación
    return when {
        resolvedMime.startsWith("image") -> FormatType.IMAGE
        resolvedMime.startsWith("video") -> FormatType.VIDEO
        else -> FormatType.UNKNOWN
    }
}
