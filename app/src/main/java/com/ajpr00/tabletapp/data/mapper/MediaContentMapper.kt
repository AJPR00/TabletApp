package com.ajpr00.tabletapp.data.mapper

import com.ajpr00.tabletapp.data.db.local.entity.MediaContentEntity
import com.ajpr00.tabletapp.domain.model.FormatType
import com.ajpr00.tabletapp.domain.model.MediaContent

fun MediaContentEntity.toDomain(): MediaContent =
    MediaContent(
        id = id,
        name = name,
        path = path,
        type = FormatType.valueOf(type),
        isFavorite = isFavorite
    )

fun MediaContent.toEntity(): MediaContentEntity =
    MediaContentEntity(
        id = id,
        name = name,
        path = path,
        type = type.name,
        isFavorite = isFavorite
    )
