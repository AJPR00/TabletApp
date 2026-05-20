package com.ajpr00.mobile.data.datasource.local

import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.mobile.data.datasource.local.db.dao.DispositivoDao
import com.ajpr00.mobile.data.mapper.toDomain
import com.ajpr00.mobile.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DispositivoLocalDataSourceImpl @Inject constructor(
    private val dao: DispositivoDao
) : DispositivoLocalDataSource {

    override fun getDispositivos(): Flow<List<Dispositivo>> =
        dao.getAllDispositivos().map { entities -> entities.map { it.toDomain() } }

    override suspend fun insertDispositivo(dispositivo: Dispositivo) =
        dao.insertDispositivo(dispositivo.toEntity())

    override suspend fun deleteDispositivo(dispositivo: Dispositivo) =
        dao.deleteDispositivo(dispositivo.toEntity())

    override suspend fun updateDispositivo(dispositivo: Dispositivo) =
        dao.updateDispositivo(dispositivo.toEntity())
}