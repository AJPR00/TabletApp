package com.ajpr00.visumloop.mobile.data.repository

import com.ajpr00.core.domain.model.FormatType
import com.ajpr00.core.domain.model.PendingMedia
import com.ajpr00.core.domain.model.PendingStatus
import com.ajpr00.mobile.data.datasource.local.PendingMediaLocalDataSource
import com.ajpr00.mobile.data.repositoryImp.PendingMediaRepositoryImpl
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.*

/**
 * Test unitario que valida la gestión de la cola de envío (FIFO) en el módulo mobile.
 *
 * Se comprueba:
 * - Inserción de archivos en la cola.
 * - Obtención del siguiente archivo según FIFO (createdAt ASC).
 * - Eliminación tras enviarse.
 * - Actualización de estado.
 * - Incremento de reintentos.
 */
class PendingMediaRepositoryImplTest {

    private val localDS = mock<PendingMediaLocalDataSource>()
    private val repository = PendingMediaRepositoryImpl(localDS)

    private fun fakeMedia(
        id: String,
        createdAt: Long
    ) = PendingMedia(
        id = id,
        deviceId = "device1",
        filePath = "/file/$id.jpg",
        thumbnailPath = "/thumb/$id.jpg",
        type = FormatType.IMAGE,
        status = PendingStatus.PENDING,
        createdAt = createdAt,
        retries = 0
    )

    /**
     * Verifica que un archivo se añade correctamente a la cola.
     */
    @Test
    fun `repository inserts media into queue`() = runTest {
        val media = fakeMedia("1", 1000L)

        repository.insert(media)

        verify(localDS).insert(eq(media))
    }

    /**
     * Verifica que la cola mantiene el orden FIFO según createdAt ASC.
     */
    @Test
    fun `repository gets next media FIFO`() = runTest {
        val m1 = fakeMedia("1", 1000L)
        val m2 = fakeMedia("2", 2000L)

        whenever(localDS.getNextToSend()).thenReturn(m1)

        val next = repository.getNextToSend()

        assert(next == m1)
    }

    /**
     * Verifica que un archivo se elimina tras enviarse.
     */
    @Test
    fun `repository deletes media after sending`() = runTest {
        val media = fakeMedia("1", 1000L)

        repository.delete(media)

        verify(localDS).delete(eq(media))
    }

    /**
     * Verifica que updateStatus delega correctamente.
     */
    @Test
    fun `repository updates media status`() = runTest {
        repository.updateStatus("1", PendingStatus.SENT)

        verify(localDS).updateStatus(eq("1"), eq(PendingStatus.SENT))
    }

    /**
     * Verifica que incrementRetries delega correctamente.
     */
    @Test
    fun `repository increments retries`() = runTest {
        repository.incrementRetries("1")

        verify(localDS).incrementRetries(eq("1"))
    }

    /**
     * Verifica que getAll devuelve el flujo correcto.
     */
    @Test
    fun `repository gets all pending media`() = runTest {
        val list = listOf(fakeMedia("1", 1000L))

        whenever(localDS.getAll()).thenReturn(flowOf(list))

        val result = repository.getAll().first()

        assert(result.size == 1)
        assert(result.first().id == "1")
    }
}
