package com.ajpr00.visumloop.tablet.domain.usecase

import com.ajpr00.visumloop.tablet.data.repository.FtpRepository
import jakarta.inject.Inject

class LoginWithFTPUseCase @Inject constructor(
    private val repo: FtpRepository
) {
    suspend operator fun invoke(token: String): Result<Unit> {
        return repo.loginWithFTP(token)
    }

}