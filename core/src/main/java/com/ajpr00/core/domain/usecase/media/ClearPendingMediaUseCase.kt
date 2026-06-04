package com.ajpr00.core.domain.usecase.media

import com.ajpr00.core.domain.repository.media.PendingMediaRepository
import javax.inject.Inject

class ClearPendingMediaUseCase @Inject constructor(
    private val repository: PendingMediaRepository
) {
    suspend operator fun invoke() = repository.clear()
}