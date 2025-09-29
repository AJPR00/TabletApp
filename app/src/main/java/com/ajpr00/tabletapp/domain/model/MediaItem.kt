package com.ajpr00.tabletapp.domain.model

data class MediaItem(
    val id: String,
    val name: String,
    val path: String,
    val isFavorite: Boolean = false
)