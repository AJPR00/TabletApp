package com.ajpr00.visumloop.tablet.domain.usecase

import android.content.Context
import android.net.Uri
import com.ajpr00.visumloop.tablet.domain.model.MediaContent
import com.ajpr00.visumloop.tablet.util.detectFormatType
import jakarta.inject.Inject

class ProcessLocalMediaUseCase @Inject constructor() {

    operator fun invoke(context: Context, uris: List<Uri>): List<MediaContent> {
        return uris.map { uri ->
            MediaContent(
                id = 0,
                name = uri.toString(),
                path = uri.toString(),
                type = detectFormatType(context, uri),
                isFavorite = false
            )
        }
    }
}
