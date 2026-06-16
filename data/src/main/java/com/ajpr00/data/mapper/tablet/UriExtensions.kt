package com.ajpr00.data.mapper.tablet

import android.content.Context
import android.net.Uri
import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.core.domain.model.PendingMedia
import com.ajpr00.data.file.FileManager
import com.ajpr00.data.mapper.media.toMediaContent
import com.ajpr00.data.mapper.media.toPendingMedia
import com.ajpr00.data.util.detectFormatType

fun Uri.toMediaContent(context: Context): MediaContent {
    val type = detectFormatType(context, this)
    val file = FileManager.copyUriToInternalFile(context, this, type)
    return file.toMediaContent(type)
}

fun Uri.toPendingMedia(context: Context): PendingMedia {
    val type = detectFormatType(context, this)
    val file = FileManager.copyUriToInternalFile(context, this, type)
    return file.toPendingMedia(type)
}

fun List<Uri>.toMediaContentList(context: Context): List<MediaContent> =
    map { it.toMediaContent(context) }
