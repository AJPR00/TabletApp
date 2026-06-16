package com.ajpr00.data.useCase.login

import com.ajpr00.data.repository.network.DropboxRepository
import javax.inject.Inject

class LoginWithDropboxUseCase @Inject constructor(
    private val dropboxRepository: DropboxRepository
) {
    suspend operator fun invoke(token: String): Result<Unit> {
        return dropboxRepository.loginWithDropbox(token)
    }
}