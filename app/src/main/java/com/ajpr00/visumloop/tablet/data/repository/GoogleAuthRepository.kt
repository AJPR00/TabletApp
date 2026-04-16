package com.ajpr00.visumloop.tablet.data.repository

import android.content.Context
import com.ajpr00.visumloop.tablet.domain.model.UserGoogle
import kotlinx.coroutines.flow.Flow

interface GoogleAuthRepository {
    suspend fun loginWithGoogle(context: Context): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun refreshSession(): Result<Unit>
    fun getLocalUser(): Flow<UserGoogle?>
    val isLocalLoggedIn: Flow<Boolean>
}
