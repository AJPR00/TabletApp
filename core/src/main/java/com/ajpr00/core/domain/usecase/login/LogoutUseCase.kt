package com.ajpr00.core.domain.usecase.login

import com.ajpr00.core.domain.repository.login.StateAuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repo: StateAuthRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repo.logout()
}
