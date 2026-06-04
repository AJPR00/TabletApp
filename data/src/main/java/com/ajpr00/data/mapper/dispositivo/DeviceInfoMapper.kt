package com.ajpr00.data.mapper.dispositivo

import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.model.EstadoDispositivo
import com.ajpr00.core.domain.model.api.DeviceInfo

fun DeviceInfo.toDispositivo(ip: String, port: Int): Dispositivo {
    return Dispositivo(
        id = this.id,
        nombre = this.name,
        ip = ip,
        puerto = port,
        aesKey = this.token,
        estado = EstadoDispositivo.DESCONOCIDO
    )
}
