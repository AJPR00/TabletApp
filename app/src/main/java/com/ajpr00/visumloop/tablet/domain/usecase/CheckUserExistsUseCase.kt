package com.ajpr00.visumloop.tablet.domain.usecase

import com.ajpr00.visumloop.tablet.data.repository.EmailAuthRepository
import jakarta.inject.Inject

class CheckUserExistsUseCase @Inject constructor(
    private val repository: EmailAuthRepository
) {
    suspend operator fun invoke(email: String): Boolean {
        return repository.userExists(email)
    }
}
