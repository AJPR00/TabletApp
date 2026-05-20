package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.repository.PendingMediaRepository
import javax.inject.Inject

class GetNextPendingMediaUseCase @Inject constructor(
    private val repository: PendingMediaRepository
) {
    suspend operator fun invoke() = repository.getNextToSend()
}
