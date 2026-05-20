package com.ajpr00.tablet.domain.model

import com.ajpr00.core.domain.model.FormatType

data class MediaContentUI(
    val id: Int = 0,
    val name: String,
    val path: String,
    val type: FormatType,
    var isFavorite: Boolean = true
)
