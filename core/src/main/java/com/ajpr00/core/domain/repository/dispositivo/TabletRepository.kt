package com.ajpr00.core.domain.repository.dispositivo

import com.ajpr00.core.domain.model.qr.Tablet

interface TabletRepository {

    suspend fun registerTablet(
        tablet: Tablet,
        ip: String,
        puerto: Int,
        estado: String,
        ultimoUpdate: Long
    )

    suspend fun getTabletById(idTablet: String): Tablet?
}
