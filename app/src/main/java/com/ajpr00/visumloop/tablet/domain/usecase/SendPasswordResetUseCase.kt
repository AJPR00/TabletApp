package com.ajpr00.visumloop.tablet.domain.usecase

import com.ajpr00.visumloop.tablet.data.repository.EmailAuthRepository
import jakarta.inject.Inject

class SendPasswordResetUseCase @Inject constructor(
    private val repository: EmailAuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return repository.sendPasswordReset(email)
    }
}
