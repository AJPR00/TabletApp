package com.ajpr00.visumloop.tablet.data.repository

interface DropboxRepository {
    suspend fun loginWithDropbox(token: String): Result<Unit>
}