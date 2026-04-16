package com.ajpr00.visumloop.tablet.domain.usecase

import com.ajpr00.visumloop.tablet.data.repository.AuthRepository
import jakarta.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repo: AuthRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repo.logout()
}
