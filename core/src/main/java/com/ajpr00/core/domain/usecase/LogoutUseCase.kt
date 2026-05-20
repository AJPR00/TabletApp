package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repo: AuthRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repo.logout()
}
