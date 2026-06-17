package com.ajpr00.data.mapper.tablet

import com.ajpr00.core.domain.model.qr.Tablet
import com.ajpr00.data.dto.dispositivo.TabletFirestoreDto

fun TabletFirestoreDto.toDomain() = Tablet(
    id = id,
    emailUsuario = emailUsuario,
    modelo = modelo
)

fun Tablet.toFirestoreDto(
    ip: String,
    puerto: Int,
    estado: String,
    ultimoUpdate: Long
) = TabletFirestoreDto(
    id = id,
    emailUsuario = emailUsuario,
    modelo = modelo,
    ip = ip,
    puerto = puerto,
    estado = estado,
    ultimoUpdate = ultimoUpdate
)

