package com.ajpr00.tablet.data.mapper

import com.ajpr00.core.domain.model.Playlist
import com.ajpr00.core.domain.model.PlaylistWithMedia
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistEntity
import com.ajpr00.tablet.data.datasource.local.db.model.PlaylistWithMediaEntityModel

fun PlaylistEntity.toDomain() = Playlist(
    id = id,
    name = name,
    updatedAt = updatedAt
)

fun PlaylistWithMediaEntityModel.toDomain() = PlaylistWithMedia(
    playlist = playlist.toDomain(),
    media = media.map { it.toDomain() }
)
