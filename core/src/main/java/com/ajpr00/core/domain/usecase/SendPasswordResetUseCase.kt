package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.repository.EmailAuthRepository
import javax.inject.Inject

class SendPasswordResetUseCase @Inject constructor(
    private val repository: EmailAuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return repository.sendPasswordReset(email)
    }
}
