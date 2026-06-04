package com.ajpr00.core.domain.usecase.media

import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.core.domain.repository.media.MediaRepository
import javax.inject.Inject

class DeleteMediaUseCase @Inject constructor(
    private val repo: MediaRepository
) {
    suspend operator fun invoke(media: MediaContent) = repo.deleteMedia(media)
}