package com.ajpr00.visumloop.tablet.domain.usecase

import android.content.Context
import com.ajpr00.visumloop.tablet.data.repository.GoogleAuthRepository
import jakarta.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val repo: GoogleAuthRepository
) {
    suspend operator fun invoke(context: Context): Result<Unit> = repo.loginWithGoogle(context)
}
