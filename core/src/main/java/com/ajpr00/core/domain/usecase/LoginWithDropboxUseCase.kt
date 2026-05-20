package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.repository.DropboxRepository
import javax.inject.Inject

class LoginWithDropboxUseCase @Inject constructor(
    private val dropboxRepository: DropboxRepository
) {
    suspend operator fun invoke(token: String): Result<Unit> {
        return dropboxRepository.loginWithDropbox(token)
    }
}
