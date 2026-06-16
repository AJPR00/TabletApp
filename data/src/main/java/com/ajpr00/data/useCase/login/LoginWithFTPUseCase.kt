package com.ajpr00.data.useCase.login

import com.ajpr00.data.repository.network.FtpRepository
import javax.inject.Inject

class LoginWithFTPUseCase @Inject constructor(
    private val repo: FtpRepository
) {
    suspend operator fun invoke(token: String): Result<Unit> {
        return repo.loginWithFTP(token)
    }

}