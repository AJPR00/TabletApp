package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.repository.FtpRepository
import javax.inject.Inject

class LoginWithFTPUseCase @Inject constructor(
    private val repo: FtpRepository
) {
    suspend operator fun invoke(token: String): Result<Unit> {
        return repo.loginWithFTP(token)
    }

}