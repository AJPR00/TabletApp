package com.ajpr00.data.mapper.tablet

import com.ajpr00.core.domain.model.qr.Tablet
import com.ajpr00.data.dto.dispositivo.TabletFirestoreDto

fun TabletFirestoreDto.toDomain() = Tablet(
    id = id,
    emailUsuario = emailUsuario,
    modelo = modelo
)
