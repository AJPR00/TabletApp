package com.ajpr00.core.domain.repository

interface DropboxRepository {
    suspend fun loginWithDropbox(token: String): Result<Unit>
}