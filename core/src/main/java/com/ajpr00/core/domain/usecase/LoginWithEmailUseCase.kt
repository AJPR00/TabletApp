package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.repository.EmailAuthRepository
import javax.inject.Inject

class LoginWithEmailUseCase @Inject constructor(
    private val emailRepository: EmailAuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return emailRepository.loginEmail(email, password)
    }
}
