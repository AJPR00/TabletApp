package com.ajpr00.data.repository

import com.ajpr00.core.domain.model.UserAuthData
import kotlinx.coroutines.flow.Flow

interface GoogleAuthRepository {
    suspend fun logout(): Result<Unit>

    suspend fun refreshSession(): Result<Unit>

    fun getLocalUser(): Flow<UserAuthData?>

    val isLocalLoggedIn: Flow<Boolean>
}