package com.ajpr00.mobile.data.datasource.local

import com.ajpr00.core.domain.model.Dispositivo
import kotlinx.coroutines.flow.Flow

/**
 * DispositivoLocalDataSource
 *
 * Fuente de datos local encargada de acceder a la base de datos de dispositivos.
 * Define las operaciones básicas para:
 *
 *  - Obtener todos los dispositivos almacenados (Flow)
 *  - Insertar un nuevo dispositivo
 *  - Eliminar un dispositivo existente
 *  - Actualizar los datos de un dispositivo
 *
 * Es la capa más cercana a Room; el repositorio delega en este DataSource.
 */
interface DispositivoLocalDataSource {
    fun getDispositivos(): Flow<List<Dispositivo>>
    suspend fun insertDispositivo(dispositivo: Dispositivo)
    suspend fun deleteDispositivo(dispositivo: Dispositivo)
    suspend fun updateDispositivo(dispositivo: Dispositivo)
}
