package com.ajpr00.core.domain.repository.network

interface NetworkRepository {
    suspend fun hasInternetConnection(): Boolean
}
