package com.ajpr00.visumloop.tablet.domain.usecase

import com.ajpr00.visumloop.tablet.data.repository.AuthRepository
import com.ajpr00.visumloop.tablet.domain.model.UserGoogle
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveCurrentUserUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    operator fun invoke(): Flow<UserGoogle?> {
        return repo.currentUser
    }
}
