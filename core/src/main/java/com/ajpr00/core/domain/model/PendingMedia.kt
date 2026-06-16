package com.ajpr00.core.domain.model

data class PendingMedia(
    val id: String,
    val filePath: String,
    val thumbnailPath: String,
    val type: FormatType,
    val status: PendingStatus,
    val createdAt: Long,
    val retries: Int,
    var deviceId: String
)

enum class PendingStatus {
    PENDING,
    SENDING,
    FAILED,
    SENT
}
