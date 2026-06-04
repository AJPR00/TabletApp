package com.ajpr00.mobile.data.mapper

import com.ajpr00.mobile.data.datasource.local.db.entity.DispositivoEntity
import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.model.EstadoDispositivo

fun DispositivoEntity.toDomain() = Dispositivo(
    id = id,
    nombre = nombre,
    ip = ip,
    puerto = puerto,
    nivelBatery = null,
    estado = EstadoDispositivo.DESCONOCIDO,
    aesKey = aesKey
)

fun Dispositivo.toEntity() = DispositivoEntity(
    id = id,
    nombre = nombre,
    ip = ip,
    puerto = puerto,
    tipo = "unknown",
    aesKey = aesKey
)
