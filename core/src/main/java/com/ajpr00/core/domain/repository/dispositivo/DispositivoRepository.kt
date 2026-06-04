package com.ajpr00.core.domain.repository.dispositivo

import com.ajpr00.core.domain.model.Dispositivo
import kotlinx.coroutines.flow.Flow

/**
 * DispositivoRepository
 *
 * Interfaz del dominio encargada de gestionar los dispositivos guardados
 * en la aplicación. Define las operaciones básicas para:
 *
 *  - Obtener la lista de dispositivos en tiempo real (Flow)
 *  - Insertar un nuevo dispositivo
 *  - Eliminar un dispositivo existente
 *  - Actualizar los datos de un dispositivo
 *
 * La implementación real se encuentra en el módulo "data".
 */

interface DispositivoRepository {

    fun getDispositivos(): Flow<List<Dispositivo>>

    suspend fun insertDispositivo(dispositivo: Dispositivo)

    suspend fun deleteDispositivo(dispositivo: Dispositivo)

    suspend fun updateDispositivo(dispositivo: Dispositivo)
}