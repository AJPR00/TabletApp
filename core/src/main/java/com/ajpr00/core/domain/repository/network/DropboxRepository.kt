package com.ajpr00.core.domain.repository.network

interface DropboxRepository {
    suspend fun loginWithDropbox(token: String): Result<Unit>
}