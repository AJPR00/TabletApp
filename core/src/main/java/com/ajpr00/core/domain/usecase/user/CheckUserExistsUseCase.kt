package com.ajpr00.core.domain.usecase.user

import com.ajpr00.core.domain.repository.login.EmailAuthRepository
import javax.inject.Inject

class CheckUserExistsUseCase @Inject constructor(
    private val repository: EmailAuthRepository
) {
    suspend operator fun invoke(email: String): Boolean {
        return repository.userExists(email)
    }
}