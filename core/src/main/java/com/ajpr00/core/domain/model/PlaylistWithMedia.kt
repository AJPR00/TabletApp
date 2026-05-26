package com.ajpr00.core.domain.model

data class PlaylistWithMedia(
    val playlist: Playlist,
    val media: List<MediaContent>
)
