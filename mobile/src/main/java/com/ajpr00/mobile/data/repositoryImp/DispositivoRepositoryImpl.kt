package com.ajpr00.mobile.data.repositoryImp

import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.repository.DispositivoRepository
import com.ajpr00.mobile.data.datasource.local.DispositivoLocalDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DispositivoRepositoryImpl @Inject constructor(
    private val dispositivoDS: DispositivoLocalDataSource
) : DispositivoRepository {
    override fun getDispositivos(): Flow<List<Dispositivo>> = dispositivoDS.getDispositivos()

    override suspend fun insertDispositivo(dispositivo: Dispositivo) =
        dispositivoDS.insertDispositivo(dispositivo)

    override suspend fun deleteDispositivo(dispositivo: Dispositivo) =
        dispositivoDS.deleteDispositivo(dispositivo)

    override suspend fun updateDispositivo(dispositivo: Dispositivo) {
        dispositivoDS.updateDispositivo(dispositivo)
    }
}
