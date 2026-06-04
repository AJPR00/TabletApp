package com.ajpr00.core.domain.usecase.media

import com.ajpr00.core.domain.repository.media.MediaRepository
import javax.inject.Inject

class DeleteMediaSyncUseCase @Inject constructor(
    private val repo: MediaRepository
) {
    operator fun invoke(id: String) = repo.deleteSync(id)
}