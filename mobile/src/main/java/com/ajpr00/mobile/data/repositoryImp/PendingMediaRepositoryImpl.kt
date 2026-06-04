package com.ajpr00.mobile.data.repositoryImp

import com.ajpr00.core.domain.model.PendingMedia
import com.ajpr00.core.domain.model.PendingStatus
import com.ajpr00.core.domain.repository.media.PendingMediaRepository
import com.ajpr00.mobile.data.datasource.local.PendingMediaLocalDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementación de PendingMediaRepository.
 *
 * Esta clase actúa como puente entre la capa de dominio y la fuente de datos
 * local que gestiona la cola de archivos pendientes de envío. Todas las
 * operaciones (insertar, actualizar estado, obtener el siguiente archivo,
 * eliminar, etc.) se delegan al DataSource local.
 *
 * La lógica de dominio solo conoce la interfaz PendingMediaRepository; aquí
 * se encuentra la implementación real usada por el módulo mobile.
 */

class PendingMediaRepositoryImpl @Inject constructor(
    private val pendingMediaDS: PendingMediaLocalDataSource
) : PendingMediaRepository {

    override fun getAll(): Flow<List<PendingMedia>> =
        pendingMediaDS.getAll()

    override suspend fun getNextToSend(): PendingMedia? =
        pendingMediaDS.getNextToSend()

    override suspend fun insert(media: PendingMedia) =
        pendingMediaDS.insert(media)

    override suspend fun update(media: PendingMedia) =
        pendingMediaDS.update(media)

    override suspend fun updateStatus(id: String, status: PendingStatus) =
        pendingMediaDS.updateStatus(id, status)

    override suspend fun incrementRetries(id: String) =
        pendingMediaDS.incrementRetries(id)

    override suspend fun delete(media: PendingMedia) =
        pendingMediaDS.delete(media)

    override suspend fun deleteById(id: String) =
        pendingMediaDS.deleteById(id)

    override suspend fun clear() =
        pendingMediaDS.clear()
}