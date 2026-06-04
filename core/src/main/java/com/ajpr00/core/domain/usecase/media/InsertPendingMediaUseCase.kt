package com.ajpr00.core.domain.usecase.media

import com.ajpr00.core.domain.model.PendingMedia
import com.ajpr00.core.domain.repository.media.PendingMediaRepository
import javax.inject.Inject

class InsertPendingMediaUseCase @Inject constructor(
    private val repository: PendingMediaRepository
) {
    suspend operator fun invoke(media: PendingMedia) =
        repository.insert(media)
}