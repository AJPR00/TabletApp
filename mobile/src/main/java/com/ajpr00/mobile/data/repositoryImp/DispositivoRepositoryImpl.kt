package com.ajpr00.mobile.data.repositoryImp

import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.repository.dispositivo.DispositivoRepository
import com.ajpr00.mobile.data.datasource.local.DispositivoLocalDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de DispositivoRepository.
 *
 * Se encarga de gestionar los dispositivos almacenados en la base de datos
 * local. Todas las operaciones (listar, insertar, actualizar y eliminar)
 * se delegan al DataSource local.
 *
 * La capa de dominio solo conoce la interfaz; aquí se encuentra la
 * implementación concreta usada por el módulo mobile.
 */

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
