package com.ajpr00.core.domain.usecase.pairing

import com.ajpr00.core.domain.repository.pairing.PairingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePinUseCase @Inject constructor(
    private val repository: PairingRepository
) {
    operator fun invoke(): Flow<String> = repository.pinFlow
}
