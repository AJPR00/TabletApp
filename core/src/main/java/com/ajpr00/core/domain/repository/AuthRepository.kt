package com.ajpr00.core.domain.repository

import com.ajpr00.core.domain.model.UserGoogle
import kotlinx.coroutines.flow.Flow
interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    val currentUser: Flow<UserGoogle?>
    suspend fun logout(): Result<Unit>
}
