package com.ajpr00.core.domain.usecase.user

import com.ajpr00.core.domain.repository.login.AuthRepository
import javax.inject.Inject

class CheckUserExistsUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    suspend operator fun invoke(email: String): Boolean {
        return repo.userExists(email)
    }
}