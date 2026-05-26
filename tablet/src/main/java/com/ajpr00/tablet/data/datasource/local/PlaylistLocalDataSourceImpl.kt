package com.ajpr00.tablet.data.datasource.local

import com.ajpr00.tablet.data.datasource.local.db.dao.PlaylistDao
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistEntity
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistMediaCrossRef
import com.ajpr00.tablet.data.datasource.local.db.model.PlaylistWithMediaEntityModel
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class PlaylistLocalDataSourceImpl @Inject constructor(
    private val playlistDao: PlaylistDao
) : PlaylistLocalDataSource {

    /**
     * Devuelve todas las playlists almacenadas en la BD.
     */
    override fun getAll(): Flow<List<PlaylistEntity>> = playlistDao.getAll()

    /**
     * Elimina una playlist por ID.
     * Room se encarga de borrar también sus relaciones si están configuradas.
     */
    override suspend fun deletePlaylist(id: String) = playlistDao.deletePlaylist(id)

    /**
     * Elimina una relación playlist–media concreta.
     */
    override suspend fun deleteCrossRef(playlistId: String, mediaId: String) =
        playlistDao.deleteCrossRef(playlistId, mediaId)

    /**
     * Inserta una playlist en la BD.
     */
    override suspend fun insertPlaylist(playlist: PlaylistEntity) =
        playlistDao.insertPlaylist(playlist)

    /**
     * Inserta una única relación playlist–media.
     */
    override suspend fun insertCrossRef(crossRef: PlaylistMediaCrossRef) =
        playlistDao.insertCrossRef(crossRef)

    /**
     * Obtiene una playlist junto con todas sus medias asociadas.
     */
    override suspend fun getPlaylistWithMedia(id: String): PlaylistWithMediaEntityModel =
        playlistDao.getPlaylistWithMedia(id)

    /**
     * Inserta una lista completa de relaciones playlist–media.
     * Se usa para operaciones batch (importaciones masivas).
     */
    override suspend fun insertCrossRefs(crossRefs: List<PlaylistMediaCrossRef>) {
        playlistDao.insertCrossRefs(crossRefs)
    }

    override suspend fun getOrCreatePlaylist(name: String): String {
        val existing = playlistDao.getPlaylistByName(name)
        if (existing != null) return existing.id

        val playlist =PlaylistEntity(
            name = name,
            updatedAt = System.currentTimeMillis()
        )
        playlistDao.insertPlaylist(playlist)
        return playlist.id
    }

}
