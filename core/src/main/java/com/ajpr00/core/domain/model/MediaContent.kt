package com.ajpr00.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MediaContent(
    val id: Int = 0,
    val name: String,
    val path: String,
    val type: FormatType,
)

enum class FormatType {
    IMAGE,
    VIDEO,
    UNKNOWN
}