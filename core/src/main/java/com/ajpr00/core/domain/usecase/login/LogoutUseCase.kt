package com.ajpr00.core.domain.usecase.login

import com.ajpr00.core.domain.repository.preference.SessionManager
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repo: SessionManager,
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            repo.clearAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
