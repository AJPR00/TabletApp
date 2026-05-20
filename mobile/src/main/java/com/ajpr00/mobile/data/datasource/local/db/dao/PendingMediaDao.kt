package com.ajpr00.mobile.data.datasource.local.db.dao

import androidx.room.*
import com.ajpr00.mobile.data.datasource.local.db.entity.PendingMediaEntity
import com.ajpr00.core.domain.model.PendingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingMediaDao {

    // Obtener todos los pendientes ordenados por fecha
    @Query("SELECT * FROM pending_media ORDER BY createdAt ASC")
    fun getAll(): Flow<List<PendingMediaEntity>>

    // Obtener solo los que están pendientes o fallados (para el Worker)
    @Query("""
        SELECT * FROM pending_media 
        WHERE status = :pending OR status = :failed
        ORDER BY createdAt ASC
        LIMIT 1
    """)
    suspend fun getNextToSend(
        pending: PendingStatus = PendingStatus.PENDING,
        failed: PendingStatus = PendingStatus.FAILED
    ): PendingMediaEntity?

    // Insertar un nuevo registro
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: PendingMediaEntity)

    // Insertar varios
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<PendingMediaEntity>)

    // Actualizar un registro
    @Update
    suspend fun update(media: PendingMediaEntity)

    // Cambiar estado
    @Query("UPDATE pending_media SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: PendingStatus)

    // Incrementar reintentos
    @Query("UPDATE pending_media SET retries = retries + 1 WHERE id = :id")
    suspend fun incrementRetries(id: String)

    // Borrar un registro
    @Delete
    suspend fun delete(media: PendingMediaEntity)

    // Borrar por ID
    @Query("DELETE FROM pending_media WHERE id = :id")
    suspend fun deleteById(id: String)

    // Vaciar tabla
    @Query("DELETE FROM pending_media")
    suspend fun clear()
}
