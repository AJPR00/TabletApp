package com.ajpr00.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun validarDocumentoIdentidad(documento: String): Boolean {
    val dniRegex = Regex("^[0-9]{8}[A-Za-z]$")
    val nieRegex = Regex("^[XxYyZz][0-9]{7}[A-Za-z]$")
    return documento.matches(dniRegex) || documento.matches(nieRegex)
}

fun validarTelefono(telefono: String): Boolean {
    val regex = Regex("^[6789]\\d{8}$")
    return telefono.matches(regex)
}

fun validarPassword(password: String): Boolean {
    val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&]).{8,}$")
    return password.matches(passwordRegex)
}

fun validarRegistro(registro: String): Boolean {
    return registro.matches(Regex("""\d{4}-E-(RE|RC)-\d{4}""", RegexOption.IGNORE_CASE))
}

fun validarEmail(email: String?): Boolean {
    if (email.isNullOrBlank()) return false
    val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    return regex.matches(email)
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

fun limitDeviceName(input: String, maxLength: Int = 20): Boolean {
    return input.matches(Regex("^[a-zA-Z0-9 ]{3,$maxLength}$"))
}

fun isValidIpv4(input: String): Boolean {
    val ipv4Regex = Regex(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}$"
    )
    return ipv4Regex.matches(input)
}









