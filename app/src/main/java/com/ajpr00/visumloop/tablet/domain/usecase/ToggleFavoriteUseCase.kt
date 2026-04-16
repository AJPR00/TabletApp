package com.ajpr00.visumloop.tablet.domain.usecase

import com.ajpr00.visumloop.tablet.data.repository.MediaRepository
import com.ajpr00.visumloop.tablet.domain.model.MediaContent
import jakarta.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repo: MediaRepository
) {
    suspend operator fun invoke(media: MediaContent) {
        if (media.isFavorite) {
            repo.deleteMedia(media)
        } else {
            repo.addBd(media.copy(isFavorite = true))
        }
    }
}
