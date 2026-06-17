package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.model.qr.Tablet
import com.ajpr00.core.domain.repository.dispositivo.TabletRepository
import javax.inject.Inject

class RegisterFirestoreTabletUseCase @Inject constructor(
    private val repository: TabletRepository
) {
    suspend operator fun invoke(
        tablet: Tablet,
        ip: String,
        puerto: Int,
        estado: String,
        ultimoUpdate: Long
    ) {
        repository.registerTablet(
            tablet = tablet,
            ip = ip,
            puerto = puerto,
            estado = estado,
            ultimoUpdate = ultimoUpdate
        )
    }
}