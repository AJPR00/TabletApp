package com.ajpr00.core.domain.repository

import com.ajpr00.core.domain.model.UserGoogle
import kotlinx.coroutines.flow.Flow

interface GoogleAuthRepository {

    suspend fun loginWithGoogle(): Result<Unit>

    suspend fun logout(): Result<Unit>

    suspend fun refreshSession(): Result<Unit>

    fun getLocalUser(): Flow<UserGoogle?>

    val isLocalLoggedIn: Flow<Boolean>
}