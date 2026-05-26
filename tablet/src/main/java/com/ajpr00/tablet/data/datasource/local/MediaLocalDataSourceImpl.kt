package com.ajpr00.tablet.data.datasource.local

import com.ajpr00.tablet.data.datasource.local.db.dao.MediaContentDao
import com.ajpr00.tablet.data.datasource.local.db.dao.PlaylistDao
import com.ajpr00.tablet.data.datasource.local.db.entity.MediaContentEntity
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistEntity
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistMediaCrossRef
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MediaLocalDataSourceImpl @Inject constructor(
    private val mediaDao: MediaContentDao,
    private val playlistDao: PlaylistDao
) : MediaLocalDataSource {
    override suspend fun insertAll(medias: List<MediaContentEntity>) {
        mediaDao.insertAll(medias)
    }

    override fun getAll(): Flow<List<MediaContentEntity>> = mediaDao.getAllMedia()

    suspend fun insertMediaListIntoPlaylist(
        medias: List<MediaContentEntity>,
        playlist: PlaylistEntity
    ) {
        // 1. Insertar todos los medias
        mediaDao.insertAll(medias)

        // 2. Insertar playlist (si no existe)
        playlistDao.insertPlaylist(playlist)

        // 3. Insertar todas las relaciones N:M
        val crossRefs = medias.mapIndexed { index, media ->
            PlaylistMediaCrossRef(
                playlistId = playlist.id,
                mediaId = media.id,
                position = index
            )
        }

        crossRefs.forEach { playlistDao.insertCrossRef(it) }
    }

    suspend fun insertMediaIntoPlaylist(
        media: MediaContentEntity,
        playlist: PlaylistEntity
    ) {
        // 1. Insertar media
        mediaDao.insert(media)

        // 2. Insertar playlist (si no existe)
        playlistDao.insertPlaylist(playlist)

        // 3. Insertar relación N:M
        val crossRef = PlaylistMediaCrossRef(
            playlistId = playlist.id,
            mediaId = media.id,
            position = 0
        )
        playlistDao.insertCrossRef(crossRef)
    }

    override suspend fun insert(media: MediaContentEntity) = mediaDao.insert(media)
    override suspend fun delete(media: MediaContentEntity) = mediaDao.delete(media)
    override suspend fun getById(id: String): MediaContentEntity? = mediaDao.getById(id)
    override suspend fun getAllOnce() = mediaDao.getAllOnce()
    override suspend fun deleteById(id: Int) = mediaDao.deleteById(id)
}
