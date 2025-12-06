package com.ajpr00.visumloop.tablet.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ajpr00.visumloop.tablet.domain.model.FormatType


/**
 * Detecta el tipo de formato de un archivo.
 *
 * Flujo de funcionamiento:
 * Entrada:
 *  - Puede venir de dos formas:
 *    a) Local (Android): se pasa un Context y un Uri → el sistema devuelve el MIME con ContentResolver.
 *    b) Cloud (Drive, Dropbox, etc.): se pasa directamente el MIME porque ya contiene el context.
 *
 * Detección:
 *  - Si tenemos un MIME directo (parámetro mime), lo usamos.
 *  - Si no, y tenemos Context + Uri, pedimos al sistema el MIME del archivo.
 *  - Si no conseguimos MIME, devolvemos por defecto FormatType.IMAGE.
 *
 * Clasificación:
 *  - Si el MIME empieza por "image" → FormatType.IMAGE
 *  - Si empieza por "video" → FormatType.VIDEO
 *  - Si empieza por "audio" → FormatType.AUDIO
 *  - Si no coincide con ninguno, lanzamos una excepción para dejar claro el error.
 *
 * Salida:
 *  - Devuelve un FormatType que unifica la lógica en tu dominio.
 */
fun detectFormatType(
    context: Context? = null,   // Si trabajamos con archivos locales, necesitamos el Context
    uri: Uri? = null,           // El Uri apunta al archivo en el dispositivo,
    mime: String? = null        // Si viene de la nube (Drive, Dropbox), ya tenemos el MIME directo
): FormatType {

    val mimeLocal = mime ?: if (context != null && uri != null) {
        context.contentResolver.getType(uri)
    } else null

    if (mimeLocal == null) return FormatType.IMAGE

    Log.d("MimeType", "Clasificando: $mimeLocal")

    return when {
        mimeLocal.startsWith("image") -> FormatType.IMAGE
        mimeLocal.startsWith("video") -> FormatType.VIDEO
        mimeLocal.startsWith("audio") -> FormatType.AUDIO
        else -> throw Exception("Formato no reconocido: $mimeLocal")
    }
}


