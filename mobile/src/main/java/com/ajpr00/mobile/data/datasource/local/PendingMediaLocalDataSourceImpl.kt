package com.ajpr00.mobile.data.datasource.local

import com.ajpr00.core.domain.model.PendingMedia
import com.ajpr00.mobile.data.datasource.local.db.dao.PendingMediaDao
import com.ajpr00.core.domain.model.PendingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import toDomain
import toEntity
import javax.inject.Inject
class PendingMediaLocalDataSourceImpl @Inject constructor(
    private val dao: PendingMediaDao
) : PendingMediaLocalDataSource {

    override fun getAll(): Flow<List<PendingMedia>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getNextToSend(): PendingMedia? =
        dao.getNextToSend()?.toDomain()

    override suspend fun insert(entity: PendingMedia) =
        dao.insert(entity.toEntity())

    override suspend fun insertAll(list: List<PendingMedia>) =
        dao.insertAll(list.map { it.toEntity() })

    override suspend fun update(entity: PendingMedia) =
        dao.update(entity.toEntity())

    override suspend fun updateStatus(id: String, status: PendingStatus) =
        dao.updateStatus(id, status)

    override suspend fun incrementRetries(id: String) =
        dao.incrementRetries(id)

    override suspend fun delete(entity: PendingMedia) =
        dao.delete(entity.toEntity())

    override suspend fun deleteById(id: String) =
        dao.deleteById(id)

    override suspend fun clear() =
        dao.clear()
}
