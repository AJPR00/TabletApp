package com.ajpr00.core.domain.repository.login

interface FacebookAuthRepository {
    suspend fun loginWithFacebook(token: String): Result<Unit>
}
