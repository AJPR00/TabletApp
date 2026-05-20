package com.ajpr00.core.domain.usecase

import com.ajpr00.core.domain.repository.DispositivoRepository
import javax.inject.Inject

class GetDispositivosUseCase @Inject constructor(
    private val repository: DispositivoRepository
) {
    operator fun invoke() = repository.getDispositivos()
}
