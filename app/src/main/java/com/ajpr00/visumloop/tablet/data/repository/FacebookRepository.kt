package com.ajpr00.visumloop.tablet.data.repository

interface FacebookRepository {
    suspend fun loginWithFacebook(token: String): Result<Unit>
}
