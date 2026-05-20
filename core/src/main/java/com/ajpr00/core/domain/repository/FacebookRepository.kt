package com.ajpr00.core.domain.repository

interface FacebookRepository {
    suspend fun loginWithFacebook(token: String): Result<Unit>
}
