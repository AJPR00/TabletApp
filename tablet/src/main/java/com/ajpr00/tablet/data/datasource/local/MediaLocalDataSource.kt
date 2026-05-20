package com.ajpr00.tablet.data.datasource.local

import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.tablet.data.datasource.local.db.entity.MediaContentEntity
import kotlinx.coroutines.flow.Flow

interface MediaLocalDataSource {
    fun getAll(): Flow<List<MediaContentEntity>>
    suspend fun insert(media: MediaContent)
    suspend fun delete(media: MediaContent)
}
