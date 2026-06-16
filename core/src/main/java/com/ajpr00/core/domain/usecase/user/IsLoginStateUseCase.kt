package com.ajpr00.core.domain.usecase.user

import com.ajpr00.core.domain.repository.preference.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class IsLoginStateUseCase @Inject constructor(
    private val repo: SessionManager
) {
    operator fun invoke(): Flow<Boolean> =
        combine(
            repo.getLocalEmail(),
            repo.getLocalIdToken()
        ) { email, token ->
            email.isNotBlank() && !token.isNullOrBlank()
        }
}
