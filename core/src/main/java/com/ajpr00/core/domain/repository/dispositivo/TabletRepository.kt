package com.ajpr00.core.domain.repository.dispositivo

import com.ajpr00.core.domain.model.qr.Tablet

interface TabletRepository {
    suspend fun getTabletById(idTablet: String): Tablet?
}
