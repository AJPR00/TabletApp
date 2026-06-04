package com.ajpr00.core.domain.repository.login

import com.ajpr00.core.domain.model.UserGoogle
import kotlinx.coroutines.flow.Flow

interface StateAuthRepository {
    val isLoggedIn: Flow<Boolean>
    val currentUser: Flow<UserGoogle?>
    suspend fun logout(): Result<Unit>
}