package com.ajpr00.mobile.data.datasource.local

import com.ajpr00.core.domain.model.PendingMedia
import com.ajpr00.core.domain.model.PendingStatus
import kotlinx.coroutines.flow.Flow

interface PendingMediaLocalDataSource {

    fun getAll(): Flow<List<PendingMedia>>

    suspend fun getNextToSend(): PendingMedia?

    suspend fun insert(entity: PendingMedia)

    suspend fun insertAll(list: List<PendingMedia>)

    suspend fun update(entity: PendingMedia)

    suspend fun updateStatus(id: String, status: PendingStatus)

    suspend fun incrementRetries(id: String)

    suspend fun delete(entity: PendingMedia)

    suspend fun deleteById(id: String)

    suspend fun clear()
}
