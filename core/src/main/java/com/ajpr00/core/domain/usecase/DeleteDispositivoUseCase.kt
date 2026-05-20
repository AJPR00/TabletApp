package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.repository.DispositivoRepository
import javax.inject.Inject

class DeleteDispositivoUseCase @Inject constructor(
    private val repository: DispositivoRepository
) {
    suspend operator fun invoke(dispositivo: Dispositivo) =
        repository.deleteDispositivo(dispositivo)
}
