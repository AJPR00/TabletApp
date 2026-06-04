package com.ajpr00.core.domain.model.api

import com.ajpr00.core.domain.model.FormatType

data class MediaItemResponse(
    val id: String,
    val name: String,
    val type: FormatType
)