package com.ajpr00.core.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class MediaContent(
    val id: String= UUID.randomUUID().toString(),
    val name: String,
    val path: String,
    val type: FormatType,
    val playlistName: String = "Default"
)

enum class FormatType {
    IMAGE,
    VIDEO,
    UNKNOWN
}