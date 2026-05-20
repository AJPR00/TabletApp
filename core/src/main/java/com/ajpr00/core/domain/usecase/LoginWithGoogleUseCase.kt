package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.repository.GoogleAuthRepository
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val repo: GoogleAuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = repo.loginWithGoogle()
}
