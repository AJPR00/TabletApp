package com.ajpr00.visumloop.tablet.domain.usecase

import com.ajpr00.visumloop.tablet.data.repository.EmailAuthRepository
import jakarta.inject.Inject

class LoginWithEmailUseCase @Inject constructor(
    private val emailRepository: EmailAuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return emailRepository.loginEmail(email, password)
    }
}
