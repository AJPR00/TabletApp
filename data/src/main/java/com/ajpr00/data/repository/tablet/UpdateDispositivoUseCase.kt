package com.ajpr00.data.repository.tablet

import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.repository.dispositivo.DispositivoRepository
import javax.inject.Inject

class UpdateDispositivoUseCase @Inject constructor(
    private val repo: DispositivoRepository
) {
    suspend operator fun invoke(dispositivo: Dispositivo) =
        repo.updateDispositivo(dispositivo)
}