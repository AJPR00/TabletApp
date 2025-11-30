package com.ajpr00.tabletapp.data.repository

import com.ajpr00.tabletapp.data.db.local.dao.MediaContentDao
import com.ajpr00.tabletapp.data.mapper.toDomain
import com.ajpr00.tabletapp.data.mapper.toEntity
import com.ajpr00.tabletapp.domain.model.MediaContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MediaRepository @Inject constructor(
    private val dao: MediaContentDao
) {
    fun getAllMedia(): Flow<List<MediaContent>> =
        dao.getAllMedia().map { list -> list.map { it.toDomain() } }

    // Obtiene todas las medias de la base de datos
    /*suspend fun getAllMedia(): List<MediaContent> =
        dao.getAll().map { it.toDomain() }*/

    // Inserta una media en la base de datos
    suspend fun insertMedia(media: MediaContent) =
        dao.insert(media.toEntity())

    suspend fun deleteMedia(media: MediaContent) =
        dao.delete(media.toEntity())
}
