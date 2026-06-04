package com.ajpr00.core.domain.repository.dispositivo

import com.ajpr00.core.domain.model.Dispositivo
import kotlinx.coroutines.flow.Flow

interface TabletLocatorRepository {
    suspend fun locateByLan(port: Int): Flow<String>
    suspend fun locateByManual(ip: String, port: Int): String?
}