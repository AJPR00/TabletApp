package com.ajpr00.core.domain.repository.login

interface EmailAuthRepository {

    suspend fun loginEmail(email: String, password: String): Result<Unit>

    suspend fun registerEmail(email: String, password: String): Result<Unit>

    suspend fun userExists(email: String): Boolean

    suspend fun sendPasswordReset(email: String): Result<Unit>

    suspend fun getCurrentUserToken(): String
}