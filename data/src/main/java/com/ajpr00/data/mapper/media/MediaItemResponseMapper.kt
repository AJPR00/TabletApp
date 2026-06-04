package com.ajpr00.data.mapper.media

import com.ajpr00.core.domain.model.api.MediaItemResponse
import com.ajpr00.core.domain.model.RemoteMedia

fun MediaItemResponse.toDomain(thumbnail: ByteArray) = RemoteMedia(
    id = id,
    name = name,
    type = type,
    thumbnailBytes = thumbnail
)
