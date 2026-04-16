package com.ajpr00.visumloop.tablet.domain.usecase

import com.ajpr00.visumloop.tablet.data.repository.DropboxRepository
import jakarta.inject.Inject


class LoginWithDropboxUseCase @Inject constructor(
    private val dropboxRepository: DropboxRepository
) {
    suspend operator fun invoke(token: String): Result<Unit> {
        return dropboxRepository.loginWithDropbox(token)
    }
}
