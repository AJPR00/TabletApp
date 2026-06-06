package com.ajpr00.tabletapp.data.repository

import com.ajpr00.tablet.data.datasource.local.PlaylistLocalDataSource
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistEntity
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistMediaCrossRef
import com.ajpr00.tablet.data.datasource.local.db.model.PlaylistWithMediaEntityModel
import com.ajpr00.tablet.data.repositoryImp.PlaylistRepositoryImpl
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Test unitario que valida la lógica previa a la persistencia en Room
 * para el repositorio de playlists del módulo tablet.
 *
 * Este test NO usa Room real. Se mockea el DataSource local para comprobar:
 * - Delegación correcta de operaciones CRUD.
 * - Transformación dominio ↔ entidad.
 * - Inserción de relaciones many-to-many.
 * - Obtención de playlists con sus medias asociadas.
 *
 * Importancia:
 * La capa de dominio solo conoce la interfaz PlaylistRepository.
 * Este test garantiza que la implementación concreta usada por la tablet
 * funciona correctamente sin depender de la base de datos real.
 */
class PlaylistRepositoryImplTest {

    private val localDS = mock<PlaylistLocalDataSource>()
    private val repository = PlaylistRepositoryImpl(localDS)

    /**
     * Verifica que getAllPlaylists() obtiene los datos del DataSource local
     * y los transforma correctamente a modelos de dominio.
     */
    @Test
    fun `repository gets all playlists`() = runTest {
        val entities = listOf(
            PlaylistEntity(id = "1", name = "Favoritos", updatedAt = 12345L)
        )

        whenever(localDS.getAll()).thenReturn(flowOf(entities))

        val result = repository.getAllPlaylists().first()

        assert(result.size == 1)
        assert(result.first().id == "1")
        assert(result.first().name == "Favoritos")
    }

    /**
     * Verifica que createPlaylist() inserta una PlaylistEntity generada automáticamente.
     */
    @Test
    fun `repository creates playlist`() = runTest {
        repository.createPlaylist("Nueva Lista")

        verify(localDS).insertPlaylist(
            argThat { name == "Nueva Lista" }
        )
    }

    /**
     * Verifica que deletePlaylist() delega correctamente la eliminación.
     */
    @Test
    fun `repository deletes playlist`() = runTest {
        repository.deletePlaylist("1")

        verify(localDS).deletePlaylist(eq("1"))
    }

    /**
     * Verifica que addMediaToPlaylist() inserta correctamente un CrossRef.
     */
    @Test
    fun `repository adds media to playlist`() = runTest {
        repository.addMediaToPlaylist(mediaId = "M1", playlistId = "P1")

        verify(localDS).insertCrossRef(
            eq(
                PlaylistMediaCrossRef(
                    playlistId = "P1",
                    mediaId = "M1",
                    position = 0
                )
            )
        )
    }

    /**
     * Verifica que addMediaListToPlaylistInternal() genera CrossRefs con posiciones correctas.
     */
    @Test
    fun `repository adds media list to playlist`() = runTest {
        val mediaIds = listOf("M1", "M2", "M3")

        repository.addMediaListToPlaylistInternal("P1", mediaIds)

        val expected = mediaIds.mapIndexed { index, id ->
            PlaylistMediaCrossRef(
                playlistId = "P1",
                mediaId = id,
                position = index
            )
        }

        verify(localDS).insertCrossRefs(eq(expected))
    }

    /**
     * Verifica que getPlaylistWithMedia() transforma correctamente el modelo relacional.
     */
    @Test
    fun `repository gets playlist with media`() = runTest {
        val entity = PlaylistWithMediaEntityModel(
            playlist = PlaylistEntity(id = "1", name = "Favoritos"),
            media = emptyList()
        )

        whenever(localDS.getPlaylistWithMedia("1")).thenReturn(entity)

        val result = repository.getPlaylistWithMedia("1")

        assert(result.playlist.id == "1")
        assert(result.playlist.name == "Favoritos")
        assert(result.media.isEmpty())
    }
}
