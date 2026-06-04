package com.ajpr00.core.domain.usecase.login

import com.ajpr00.core.domain.repository.login.GoogleAuthRepository
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val repo: GoogleAuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = repo.loginWithGoogle()
}
