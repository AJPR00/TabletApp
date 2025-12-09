package com.ajpr00.visumloop.tablet.util

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Patterns
import com.ajpr00.visumloop.tablet.domain.model.FormatType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


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


fun validarDocumentoIdentidad(documento: String): Boolean {
    val dniRegex = Regex("^[0-9]{8}[A-Za-z]$")
    val nieRegex = Regex("^[XxYyZz][0-9]{7}[A-Za-z]$")
    return documento.matches(dniRegex) || documento.matches(nieRegex)
}

fun validarTelefono(telefono: String): Boolean {
    return Patterns.PHONE.matcher(telefono).matches()
}

fun validarPassword(password: String): Boolean {
    val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&]).{8,}$")
    return password.matches(passwordRegex)
}

fun validarRegistro(registro: String): Boolean {
    return registro.matches(Regex("""\d{4}-E-(RE|RC)-\d{4}""", RegexOption.IGNORE_CASE))
}

fun validarEmail(email: String?): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email?: "").matches()
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

fun isMenorEdad(nacimientoMillis: Long?, anioCarnaval: Int = 2025): Boolean {
    if (nacimientoMillis == null) return false

    val nacimiento = Calendar.getInstance().apply {
        timeInMillis = nacimientoMillis
    }

    val fechaCorte = Calendar.getInstance().apply {
        set(anioCarnaval, Calendar.FEBRUARY, 1, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    var edad = fechaCorte.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)
    if (fechaCorte.get(Calendar.DAY_OF_YEAR) < nacimiento.get(Calendar.DAY_OF_YEAR)) {
        edad--
    }

    return edad < 18
}



