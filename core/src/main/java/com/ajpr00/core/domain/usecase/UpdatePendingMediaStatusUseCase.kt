package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.model.PendingStatus
import com.ajpr00.core.domain.repository.PendingMediaRepository
import javax.inject.Inject

class UpdatePendingMediaStatusUseCase @Inject constructor(
    private val repository: PendingMediaRepository
) {
    suspend operator fun invoke(id: String, status: PendingStatus) =
        repository.updateStatus(id, status)
}
