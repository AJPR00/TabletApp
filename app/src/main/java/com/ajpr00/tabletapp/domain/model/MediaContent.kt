package com.ajpr00.tabletapp.domain.model

data class MediaContent(
    val id: Int = 0,
    val name: String,
    val path: String,
    val type: FormatType,   // 👈 ojo: si es enum, hay que convertirlo
    val isFavorite: Boolean = false
)
