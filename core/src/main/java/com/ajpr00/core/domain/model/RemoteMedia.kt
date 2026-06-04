package com.ajpr00.core.domain.model

data class RemoteMedia(
    val id: String,
    val name: String,
    val type: FormatType,
    val thumbnailBytes: ByteArray
)