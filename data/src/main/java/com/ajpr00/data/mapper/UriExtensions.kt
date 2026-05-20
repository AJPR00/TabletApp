package com.ajpr00.data.mapper

import android.content.Context
import android.net.Uri
import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.data.file.FileManager
import com.ajpr00.data.util.detectFormatType

fun Uri.toMediaContent(context: Context): MediaContent {
    val type = detectFormatType(context, this)
    val file = FileManager.copyUriToInternalFile(context, this, type)
    return file.toMediaContent(type)
}

fun List<Uri>.toMediaContentList(context: Context): List<MediaContent> =
    map { it.toMediaContent(context) }
