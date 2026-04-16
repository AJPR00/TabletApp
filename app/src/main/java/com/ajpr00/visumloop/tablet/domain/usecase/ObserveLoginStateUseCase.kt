package com.ajpr00.visumloop.tablet.domain.usecase

import com.ajpr00.visumloop.tablet.data.repository.AuthRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveLoginStateUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> = repo.isLoggedIn
}
