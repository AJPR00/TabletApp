package com.ajpr00.visumloop.tablet.data.mapper

import com.ajpr00.visumloop.tablet.data.datasource.local.db.entity.MediaContentEntity
import com.ajpr00.visumloop.tablet.domain.model.FormatType
import com.ajpr00.visumloop.tablet.domain.model.MediaContent

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
