package com.ajpr00.tablet.data.repositoryImp

import com.ajpr00.core.domain.model.Playlist
import com.ajpr00.core.domain.model.PlaylistWithMedia
import com.ajpr00.core.domain.repository.media.PlaylistRepository
import com.ajpr00.tablet.data.datasource.local.PlaylistLocalDataSource
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistEntity
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistMediaCrossRef
import com.ajpr00.tablet.data.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


/**
 * Implementación de PlaylistRepository.
 *
 * Gestiona todas las operaciones relacionadas con las playlists en el módulo mobile/tablet:
 *  - Leer, crear y eliminar playlists
 *  - Añadir o quitar medias dentro de una playlist
 *  - Obtener una playlist junto con todos sus medias asociados
 *  - Insertar listas completas de medias de forma eficiente
 *
 * Actúa como puente entre la capa de dominio y las fuentes de datos reales
 * (BD local, DAOs, mappers). La lógica de dominio solo conoce la interfaz;
 * aquí se encuentra la implementación concreta.
 */

class PlaylistRepositoryImpl @Inject constructor(
    private val playlistLocalDS: PlaylistLocalDataSource
) : PlaylistRepository {

    /**
     * Obtiene todas las playlists almacenadas en la base de datos.
     * Devuelve un Flow que emite actualizaciones en tiempo real.
     */
    override fun getAllPlaylists(): Flow<List<Playlist>> =
        playlistLocalDS.getAll().map { list -> list.map { it.toDomain() } }

    /**
     * Crea una nueva playlist con el nombre indicado.
     * El ID (UUID) se genera automáticamente en PlaylistEntity.
     */
    override suspend fun createPlaylist(name: String) {
        playlistLocalDS.insertPlaylist(PlaylistEntity(name = name))
    }

    /**
     * Elimina una playlist por su ID.
     * También elimina automáticamente sus relaciones con medias.
     */
    override suspend fun deletePlaylist(id: String) {
        playlistLocalDS.deletePlaylist(id)
    }

    /**
     * Añade un único media a una playlist.
     * Inserta una relación many-to-many en la tabla CrossRef.
     */
    override suspend fun addMediaToPlaylist(mediaId: String, playlistId: String) {
        playlistLocalDS.insertCrossRef(
            PlaylistMediaCrossRef(
                playlistId = playlistId,
                mediaId = mediaId,
                position = 0
            )
        )
    }

    /**
     * Añade una lista completa de medias a una playlist en una sola operación.
     * Se usa para importaciones masivas o sincronización de listas.
     * Cada media se inserta con un índice (position) incremental.
     */
    override suspend fun addMediaListToPlaylistInternal(
        playlistId: String,
        mediaIds: List<String>
    ) {
        val crossRefs = mediaIds.mapIndexed { index, id ->
            PlaylistMediaCrossRef(
                playlistId = playlistId,
                mediaId = id,
                position = index
            )
        }

        playlistLocalDS.insertCrossRefs(crossRefs)
    }

    override suspend fun getOrCreatePlaylist(name: String): String {
        return playlistLocalDS.getOrCreatePlaylist(name)
    }

    /**
     * Elimina la relación entre un media y una playlist.
     * No elimina el media ni la playlist, solo la asociación.
     */
    override suspend fun removeMediaFromPlaylist(
        playlistId: String,
        mediaId: String
    ) {
        playlistLocalDS.deleteCrossRef(playlistId, mediaId)
    }

    /**
     * Obtiene una playlist junto con todas sus medias asociadas.
     * Es una consulta compuesta que combina Playlist + MediaContent.
     */
    override suspend fun getPlaylistWithMedia(id: String): PlaylistWithMedia =
        playlistLocalDS.getPlaylistWithMedia(id).toDomain()
}
