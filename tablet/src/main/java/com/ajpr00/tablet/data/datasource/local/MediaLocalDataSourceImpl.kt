package com.ajpr00.tablet.data.datasource.local

import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.tablet.data.datasource.local.db.dao.MediaContentDao
import com.ajpr00.tablet.data.datasource.local.db.entity.MediaContentEntity
import com.ajpr00.tablet.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MediaLocalDataSourceImpl @Inject constructor(
    private val dao: MediaContentDao
) : MediaLocalDataSource {

    override fun getAll(): Flow<List<MediaContentEntity>> = dao.getAllMedia()

    override suspend fun insert(media: MediaContent) = dao.insert(media.toEntity())

    override suspend fun delete(media: MediaContent) = dao.delete(media.toEntity())
}
