package com.ajpr00.core.util

import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs

/**
 * Patrón de ordenación por proximidad
 * Ordena las IPs del rango 1..254 según lo cerca que estén
 * del host del cliente.
 *
 * Si el cliente es 10.58.241.227, empezamos por:
 * 227, 226, 228, 225, 229...
 * porque es más probable que la tablet esté cerca.
 */
fun ordenarHostsPorCercania(baseIp: String): List<Int> {
    val myHost = baseIp.split(".").last().toInt()
    return (1..254).sortedBy { host ->
        abs(host - myHost)
    }
}

/**
 * Intenta localizar una tablet en una IP concreta.
 * Devuelve TabletConnectionData si responde correctamente,
 * o null si no es una tablet válida.
 */
fun tryLocate(ip: String, port: Int): String? {
    return try {

        val url = URL("http://$ip:$port/ping")
        val conn = url.openConnection() as HttpURLConnection

        conn.connectTimeout = 1200
        conn.readTimeout = 1200
        conn.requestMethod = "GET"

        val response = conn.inputStream.bufferedReader().readText()

        if (response.trim().uppercase() == "OK") ip else null

    } catch (e: Exception) {
        null
    }
}
