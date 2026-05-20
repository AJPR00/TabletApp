package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLoginStateUseCase @Inject constructor(
    private val repo: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> = repo.isLoggedIn
}
