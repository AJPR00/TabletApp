package com.ajpr00.mobile.qr.parse

import com.ajpr00.core.domain.model.qr.QrPayload
import com.google.gson.Gson

/**
 * Parser encargado de convertir el texto bruto del QR en un objeto `QrPayload`.
 *
 * Arquitectura:
 * - Capa: data
 * - Rol: transformar el raw JSON del QR en un modelo intermedio.
 * - No crea objetos de dominio ni conoce `Dispositivo`.
 *
 * Flujo:
 * 1. Recibe el texto del QR detectado.
 * 2. Usa Gson para convertirlo a `QrPayload`.
 * 3. Devuelve el payload listo para ser mapeado.
 *
 * @param raw Texto completo del QR leído por ZXing.
 * @return Objeto `QrPayload` con los campos del QR.
 */
class QrParser {
    fun parse(raw: String): QrPayload {
        val payload = Gson().fromJson(raw, QrPayload::class.java)

        return payload
    }
}
