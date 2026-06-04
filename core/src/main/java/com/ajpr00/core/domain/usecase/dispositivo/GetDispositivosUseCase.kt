package com.ajpr00.core.domain.usecase.dispositivo

import com.ajpr00.core.domain.repository.dispositivo.DispositivoRepository
import javax.inject.Inject

class GetDispositivosUseCase @Inject constructor(
    private val repository: DispositivoRepository
) {
    operator fun invoke() = repository.getDispositivos()
}