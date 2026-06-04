package com.ajpr00.core.domain.usecase.user

import com.ajpr00.core.domain.repository.login.StateAuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLocalLoginStateUseCase @Inject constructor(
    private val repo: StateAuthRepository
) {
    operator fun invoke(): Flow<Boolean> = repo.isLoggedIn
}
