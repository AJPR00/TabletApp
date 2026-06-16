package com.ajpr00.data.repository.network

interface DropboxRepository {
    suspend fun loginWithDropbox(token: String): Result<Unit>
}