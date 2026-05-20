package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.core.domain.repository.MediaRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repo: MediaRepository
) {
    suspend operator fun invoke(media: MediaContent) {
        repo.deleteMedia(media)
    }
}

