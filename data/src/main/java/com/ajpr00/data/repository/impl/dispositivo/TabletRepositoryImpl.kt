package com.ajpr00.data.repository.impl.dispositivo

import com.ajpr00.core.domain.model.qr.Tablet
import com.ajpr00.core.domain.repository.dispositivo.TabletRepository
import com.ajpr00.data.datasource.tablet.DispositivoRemoteDataSource
import com.ajpr00.data.mapper.tablet.toDomain
import javax.inject.Inject

class TabletRepositoryImpl @Inject constructor(
    private val remote: DispositivoRemoteDataSource.TabletFirestoreDataSource
) : TabletRepository {

    override suspend fun getTabletById(idTablet: String): Tablet? {
        return remote.getTabletById(idTablet)?.toDomain()
    }
}
