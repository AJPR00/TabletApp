package com.ajpr00.core.domain.usecase.login

import com.ajpr00.core.domain.repository.login.AuthRepository
import javax.inject.Inject

class SendPasswordResetUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return repo.sendPasswordReset(email)
    }
}