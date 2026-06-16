package com.ajpr00.mobile.data.mapper

import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.model.EstadoDispositivo
import com.ajpr00.core.domain.model.mDNS.MdnsServiceInfo

fun MdnsServiceInfo.toDispositivo(): Dispositivo {
    return Dispositivo(
        id = txt,
        nombre = name,
        ip = ip,
        puerto = port,
        estado = EstadoDispositivo.ONLINE
    )
}
