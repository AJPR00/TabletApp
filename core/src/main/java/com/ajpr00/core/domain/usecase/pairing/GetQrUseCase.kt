package com.ajpr00.core.domain.usecase.pairing

import com.ajpr00.core.domain.repository.pairing.PairingRepository
import javax.inject.Inject

class GetQrUseCase @Inject constructor(
    private val repo: PairingRepository
) {
    suspend operator fun invoke() = repo.getQr()
}