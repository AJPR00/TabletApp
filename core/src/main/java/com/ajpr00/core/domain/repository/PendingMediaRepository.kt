package com.ajpr00.core.domain.repository

import com.ajpr00.core.domain.model.PendingMedia
import com.ajpr00.core.domain.model.PendingStatus
import kotlinx.coroutines.flow.Flow

interface PendingMediaRepository {
    fun getAll(): Flow<List<PendingMedia>>
    suspend fun getNextToSend(): PendingMedia?
    suspend fun insert(media: PendingMedia)
    suspend fun update(media: PendingMedia)
    suspend fun updateStatus(id: String, status: PendingStatus)
    suspend fun incrementRetries(id: String)
    suspend fun delete(media: PendingMedia)
    suspend fun deleteById(id: String)
    suspend fun clear()
}