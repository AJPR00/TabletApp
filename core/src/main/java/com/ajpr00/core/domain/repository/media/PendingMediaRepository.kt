package com.ajpr00.core.domain.repository.media

import com.ajpr00.core.domain.model.PendingMedia
import com.ajpr00.core.domain.model.PendingStatus
import kotlinx.coroutines.flow.Flow

/**
 * PendingMediaRepository
 *
 * Interfaz del dominio encargada de gestionar la cola de archivos pendientes
 * de enviar a la tablet. Permite:
 *
 *  - Obtener la lista completa de medias pendientes (Flow)
 *  - Insertar nuevos elementos en la cola
 *  - Actualizar su estado (PENDING, SENDING, SENT, ERROR…)
 *  - Incrementar reintentos de envío
 *  - Obtener el siguiente archivo que debe enviarse
 *  - Eliminar elementos individuales o limpiar toda la cola
 */
interface PendingMediaRepository {
    fun getAll(): Flow<List<PendingMedia>>
    suspend fun getNextToSend(): PendingMedia?
    suspend fun insert(media: PendingMedia)
    suspend fun update(media: PendingMedia)
    suspend fun updateStatus(id: String, status: PendingStatus)
    suspend fun incrementRetries(id: String)
    suspend fun delete(media: PendingMedia)
    suspend fun deleteById(id: String)
    suspend fun clear()
}