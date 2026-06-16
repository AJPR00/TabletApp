package com.ajpr00.data.mapper.media

import com.ajpr00.core.domain.model.FormatType
import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.core.domain.model.PendingMedia
import com.ajpr00.core.domain.model.PendingStatus
import java.io.File

fun File.toMediaContent(type: FormatType): MediaContent {
    return MediaContent(
        name = name,
        path = absolutePath,
        type = type
    )

}fun File.toPendingMedia(type: FormatType): PendingMedia {
    return PendingMedia(
        id = name,
        filePath = absolutePath,
        thumbnailPath = absolutePath,
        type = type,
        status = PendingStatus.PENDING,
        createdAt = System.currentTimeMillis(),
        retries = 0,
        deviceId = ""
    )
}