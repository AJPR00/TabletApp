package com.ajpr00.tablet.data.mapper

import com.ajpr00.core.domain.model.FormatType
import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.core.domain.model.api.MediaItemResponse
import com.ajpr00.tablet.data.datasource.local.db.entity.MediaContentEntity

fun MediaContentEntity.toDomain(): MediaContent =
    MediaContent(
        id = id,
        name = name,
        path = path,
        type = FormatType.valueOf(type),
    )

fun MediaContent.toEntity(): MediaContentEntity =
    MediaContentEntity(
        id = id,
        name = name,
        path = path,
        type = type.name,
    )

fun MediaContent.toRemote() = MediaItemResponse(
    id = id,
    name = name,
    type = type
)
