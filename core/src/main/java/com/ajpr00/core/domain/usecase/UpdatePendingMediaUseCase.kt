package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.model.PendingMedia
import com.ajpr00.core.domain.repository.PendingMediaRepository
import javax.inject.Inject

class UpdatePendingMediaUseCase @Inject constructor(
    private val repository: PendingMediaRepository
) {
    suspend operator fun invoke(media: PendingMedia) =
        repository.update(media)
}
