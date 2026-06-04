package com.ajpr00.data.mapper

import com.ajpr00.core.domain.model.FormatType
import com.ajpr00.core.domain.model.MediaContent
import java.io.File

fun File.toMediaContent(type: FormatType): MediaContent {
    return MediaContent(
        name = name,
        path = absolutePath,
        type = type
    )
}