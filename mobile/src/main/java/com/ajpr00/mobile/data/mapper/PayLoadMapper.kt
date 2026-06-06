package com.ajpr00.mobile.data.mapper

import android.util.Log
import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.model.EstadoDispositivo
import com.ajpr00.core.domain.model.qr.QrPayload

/**
 * Convierte un `QrPayload` en un objeto de dominio `Dispositivo`.
 *
 * Arquitectura:
 * - Capa: data
 * - Rol: transformar el modelo intermedio del QR en un modelo de dominio.
 * - No realiza parseo JSON ni lectura de QR; solo mapea datos.
 *
 * Flujo:
 * 1. Recibe un `QrPayload` generado por el parser.
 * 2. Construye un `Dispositivo` con los campos necesarios.
 * 3. Devuelve el objeto listo para la capa de presentación o dominio.
 *
 * Notas:
 * - El estado inicial se marca como DESCONOCIDO; la app decidirá después si está ONLINE/OFFLINE.
 */
fun QrPayload.toDomain(): Dispositivo {
    Log.d("QR_MAPPER", "toDomain: iniciando mapeo de QrPayload → Dispositivo")

    val dispositivo = Dispositivo(
        id = id,
        nombre = nombre,
        ip = ip,
        puerto = puerto,
        nivelBatery = null,
        estado = EstadoDispositivo.DESCONOCIDO,
        aesKey = aesKey
    )

    Log.d("QR_MAPPER", "toDomain: dispositivo generado → $dispositivo")

    return dispositivo
}
