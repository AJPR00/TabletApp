package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.repository.PendingMediaRepository
import javax.inject.Inject

class GetAllPendingMediaUseCase @Inject constructor(
    private val repository: PendingMediaRepository
) {
    operator fun invoke() = repository.getAll()
}
