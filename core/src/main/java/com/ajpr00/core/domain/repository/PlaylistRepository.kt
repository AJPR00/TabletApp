package com.ajpr00.core.domain.repository

import com.ajpr00.core.domain.model.Playlist
import com.ajpr00.core.domain.model.PlaylistWithMedia
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {

    /**
     * Devuelve un flujo reactivo con todas las playlists almacenadas.
     * Se actualiza automáticamente cuando cambia la base de datos.
     */
    fun getAllPlaylists(): Flow<List<Playlist>>

    /**
     * Crea una nueva playlist con el nombre indicado.
     * El repositorio se encarga de generar el ID (UUID) y persistirla.
     */
    suspend fun createPlaylist(name: String)

    /**
     * Elimina una playlist por su ID (UUID).
     * También debe eliminar sus relaciones con medias.
     */
    suspend fun deletePlaylist(id: String)

    /**
     * Añade un único media (por ID) a una playlist (por ID).
     * Representa la relación many-to-many entre playlist y media.
     */
    suspend fun addMediaToPlaylist(mediaId: String, playlistId: String)

    /**
     * Elimina un media de una playlist.
     * No elimina el media de la base de datos, solo la relación.
     */
    suspend fun removeMediaFromPlaylist(playlistId: String, mediaId: String)

    /**
     * Devuelve una playlist junto con todas sus medias asociadas.
     * Es una operación compuesta que combina Playlist + MediaContent.
     */
    suspend fun getPlaylistWithMedia(id: String): PlaylistWithMedia

    /**
     * Añade una lista completa de medias a una playlist en una sola operación.
     * Ideal para importaciones masivas o sincronización de listas.
     * Debe insertar múltiples relaciones playlist–media de forma eficiente.
     */
    suspend fun addMediaListToPlaylistInternal( playlistId: String,mediaIds: List<String>)

    suspend fun getOrCreatePlaylist(name: String): String
}
