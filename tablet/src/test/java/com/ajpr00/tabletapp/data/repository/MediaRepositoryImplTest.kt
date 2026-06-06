package com.ajpr00.tablet.data.repository

import com.ajpr00.core.domain.model.FormatType
import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.tablet.data.datasource.local.MediaLocalDataSource
import com.ajpr00.tablet.data.datasource.local.db.entity.MediaContentEntity
import com.ajpr00.tablet.data.mapper.toEntity
import com.ajpr00.tablet.data.repositoryImp.MediaRepositoryImpl
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Test unitario que valida la lógica previa a la persistencia en Room
 * para el repositorio de medios del módulo tablet.
 *
 * Este test NO usa Room real. Se mockea el DataSource local para comprobar:
 * - Que el repositorio transforma correctamente los modelos de dominio en entidades.
 * - Que delega correctamente las operaciones CRUD al DataSource.
 * - Que las conversiones entidad ↔ dominio funcionan como se espera.
 * - Que la lógica previa a la persistencia funciona de forma aislada.
 *
 * Importancia:
 * La capa de dominio solo conoce la interfaz MediaRepository.
 * Este test garantiza que la implementación concreta usada por la tablet
 * funciona correctamente sin depender de la base de datos real.
 */
class MediaRepositoryImplTest {

    private val localDS = mock<MediaLocalDataSource>()
    private val ftpDS = mock<com.ajpr00.data.datasource.cloud.FtpClientDataSource>()
    private val driveDS = mock<com.ajpr00.data.datasource.cloud.DataSourceGoogleDrive>()
    private val repository = MediaRepositoryImpl(localDS, ftpDS, driveDS)

    /**
     * Verifica que getAllMediaBd() obtiene los datos del DataSource local
     * y los transforma correctamente a modelos de dominio.
     */
    @Test
    fun `repository gets all media from local datasource`() = runTest {
        val entityList = listOf(
            MediaContentEntity(
                id = "1",
                name = "Video 1",
                path = "/storage/video1.mp4",
                type = "VIDEO"
            )
        )

        whenever(localDS.getAll()).thenReturn(flowOf(entityList))

        val result = repository.getAllMediaBd().first()

        assert(result.size == 1)
        assert(result.first().id == "1")
        assert(result.first().path == "/storage/video1.mp4")
        assert(result.first().type == FormatType.VIDEO)
    }

    /**
     * Verifica que insertAll() transforma correctamente una lista de modelos
     * de dominio en entidades y delega la inserción al DataSource local.
     */
    @Test
    fun `repository inserts all media using mapper`() = runTest {
        val domainList = listOf(
            MediaContent(
                id = "1",
                name = "Video 1",
                path = "/storage/video1.mp4",
                type = FormatType.VIDEO,
                playlistName = "Default"
            )
        )

        val expectedEntities = domainList.map { it.toEntity() }

        repository.insertAll(domainList)

        verify(localDS).insertAll(eq(expectedEntities))
    }

    /**
     * Verifica que addBd() transforma un único MediaContent en entidad
     * y lo envía al DataSource local.
     */
    @Test
    fun `repository adds single media using mapper`() = runTest {
        val media = MediaContent(
            id = "1",
            name = "Video 1",
            path = "/storage/video1.mp4",
            type = FormatType.VIDEO,
            playlistName = "Default"
        )

        val expectedEntity = media.toEntity()

        repository.addBd(media)

        verify(localDS).insert(eq(expectedEntity))
    }

    /**
     * Verifica que deleteMedia() transforma el modelo de dominio en entidad
     * y delega la eliminación al DataSource local.
     */
    @Test
    fun `repository deletes media using mapper`() = runTest {
        val media = MediaContent(
            id = "1",
            name = "Video 1",
            path = "/storage/video1.mp4",
            type = FormatType.VIDEO,
            playlistName = "Default"
        )

        val expectedEntity = media.toEntity()

        repository.deleteMedia(media)

        verify(localDS).delete(eq(expectedEntity))
    }

    /**
     * Verifica que getMediaById() obtiene una entidad desde el DataSource
     * y la transforma correctamente a modelo de dominio.
     */
    @Test
    fun `repository gets media by id and maps to domain`() = runTest {
        val entity = MediaContentEntity(
            id = "1",
            name = "Video 1",
            path = "/storage/video1.mp4",
            type = "VIDEO"
        )

        whenever(localDS.getById("1")).thenReturn(entity)

        val result = repository.getMediaById("1")

        assert(result != null)
        assert(result!!.id == "1")
        assert(result.path == "/storage/video1.mp4")
        assert(result.type == FormatType.VIDEO)
    }
}
