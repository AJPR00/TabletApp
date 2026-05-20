package com.ajpr00.mobile.data.datasource.local.db.dao

import androidx.room.*
import com.ajpr00.mobile.data.datasource.local.db.entity.DispositivoEntity
import kotlinx.coroutines.flow.Flow

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
