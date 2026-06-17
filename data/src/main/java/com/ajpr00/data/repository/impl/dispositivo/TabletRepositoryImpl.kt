package com.ajpr00.data.repository.impl.dispositivo

import com.ajpr00.core.domain.model.qr.Tablet
import com.ajpr00.core.domain.repository.dispositivo.TabletRepository
import com.ajpr00.data.datasource.tablet.TabletFirestoreDataSource
import com.ajpr00.data.mapper.tablet.toDomain
import com.ajpr00.data.mapper.tablet.toFirestoreDto
import javax.inject.Inject

class TabletRepositoryImpl @Inject constructor(
    private val remote: TabletFirestoreDataSource
) : TabletRepository {

    override suspend fun registerTablet(
        tablet: Tablet,
        ip: String,
        puerto: Int,
        estado: String,
        ultimoUpdate: Long
    ) {
        val dto = tablet.toFirestoreDto(
            ip = ip,
            puerto = puerto,
            estado = estado,
            ultimoUpdate = ultimoUpdate
        )

        remote.saveTablet(dto)
    }

    override suspend fun getTabletById(idTablet: String): Tablet? {
        return remote.getTabletById(idTablet)?.toDomain()
    }
}
