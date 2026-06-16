package com.ajpr00.data.useCase.login

import com.ajpr00.core.domain.repository.login.AuthRepository
import javax.inject.Inject

class LoginWithEmailUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return repo.loginWithEmail(email, password)
    }
}
