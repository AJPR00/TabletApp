package com.ajpr00.mobile.data.repositoryImp

import com.ajpr00.core.domain.model.PendingMedia
import com.ajpr00.core.domain.model.PendingStatus
import com.ajpr00.core.domain.repository.PendingMediaRepository
import com.ajpr00.mobile.data.datasource.local.PendingMediaLocalDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
class PendingMediaRepositoryImpl @Inject constructor(
    private val pendingMediaDS: PendingMediaLocalDataSource
) : PendingMediaRepository {

    override fun getAll(): Flow<List<PendingMedia>> =
        pendingMediaDS.getAll()

    override suspend fun getNextToSend(): PendingMedia? =
        pendingMediaDS.getNextToSend()

    override suspend fun insert(media: PendingMedia) =
        pendingMediaDS.insert(media)

    override suspend fun update(media: PendingMedia) =
        pendingMediaDS.update(media)

    override suspend fun updateStatus(id: String, status: PendingStatus) =
        pendingMediaDS.updateStatus(id, status)

    override suspend fun incrementRetries(id: String) =
        pendingMediaDS.incrementRetries(id)

    override suspend fun delete(media: PendingMedia) =
        pendingMediaDS.delete(media)

    override suspend fun deleteById(id: String) =
        pendingMediaDS.deleteById(id)

    override suspend fun clear() =
        pendingMediaDS.clear()
}