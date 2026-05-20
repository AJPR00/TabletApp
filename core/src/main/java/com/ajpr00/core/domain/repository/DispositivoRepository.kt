package com.ajpr00.core.domain.repository

import com.ajpr00.core.domain.model.Dispositivo
import kotlinx.coroutines.flow.Flow

interface DispositivoRepository {

    fun getDispositivos(): Flow<List<Dispositivo>>

    suspend fun insertDispositivo(dispositivo: Dispositivo)

    suspend fun deleteDispositivo(dispositivo: Dispositivo)

    suspend fun updateDispositivo(dispositivo: Dispositivo)
}