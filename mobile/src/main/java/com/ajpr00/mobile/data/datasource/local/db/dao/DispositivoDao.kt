package com.ajpr00.mobile.data.datasource.local.db.dao

import androidx.room.*
import com.ajpr00.mobile.data.datasource.local.db.entity.DispositivoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DispositivoDao
 *
 * DAO de Room encargado de acceder directamente a la tabla de dispositivos.
 * Proporciona las operaciones básicas de base de datos:
 *
 *  - Obtener todos los dispositivos (Flow)
 *  - Insertar un dispositivo
 *  - Eliminar un dispositivo
 *  - Actualizar un dispositivo
 *
 * Es la capa más baja del acceso a datos; el DataSource lo utiliza para
 * interactuar con la BD.
 */

@Dao
interface DispositivoDao {
    @Query("SELECT * FROM dispositivos")
    fun getAllDispositivos(): Flow<List<DispositivoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispositivo(dispositivo: DispositivoEntity)

    @Delete
    suspend fun deleteDispositivo(dispositivo: DispositivoEntity)

    @Update
    suspend fun updateDispositivo(dispositivo: DispositivoEntity)
}
