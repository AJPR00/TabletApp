package com.ajpr00.core.domain.usecase.media

import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.core.domain.repository.media.MediaRepository
import javax.inject.Inject

class GetAllMediaSyncUseCase @Inject constructor(
    private val repo: MediaRepository
) {
    operator fun invoke(): List<MediaContent> = repo.getAllSync()
}
