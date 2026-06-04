package com.ajpr00.tablet.data.datasource.local

import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.tablet.data.datasource.local.db.entity.MediaContentEntity
import kotlinx.coroutines.flow.Flow

interface MediaLocalDataSource {

    suspend fun insertAll(medias: List<MediaContentEntity>)
    fun getAll(): Flow<List<MediaContentEntity>>
    suspend fun insert(media: MediaContentEntity)
    suspend fun delete(media: MediaContentEntity)

    suspend fun getById(id: String): MediaContentEntity?
    // Métodos síncronos
    suspend fun getAllOnce(): List<MediaContentEntity>
    suspend fun deleteById(id: String)
}
