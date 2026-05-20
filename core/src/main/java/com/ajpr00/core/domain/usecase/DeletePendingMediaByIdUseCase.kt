package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.repository.PendingMediaRepository
import javax.inject.Inject

class DeletePendingMediaByIdUseCase @Inject constructor(
    private val repository: PendingMediaRepository
) {
    suspend operator fun invoke(id: String) =
        repository.deleteById(id)
}
