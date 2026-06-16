package com.ajpr00.core.domain.repository.login

import com.ajpr00.core.domain.model.UserAuthData

interface AuthRepository {
    suspend fun loginWithEmail(email: String, password: String): Result<Unit>
    suspend fun registerWithEmail(email: String, password: String): Result<Unit>
    suspend fun loginWithGoogle(idToken: String): Result<UserAuthData>
    suspend fun loginWithFacebook(token: String): Result<UserAuthData>
    suspend fun userExists(email: String): Boolean

    suspend fun sendPasswordReset(email: String): Result<Unit>

    suspend fun getCurrentUserToken(): String
    suspend fun logout(): Result<Unit>
}
