package com.ajpr00.core.domain.usecase.user

import com.ajpr00.core.domain.repository.login.AuthRepository
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return repo.registerWithEmail(email, password)
    }
}
