package com.ajpr00.core.domain.repository.pairing

import com.ajpr00.core.domain.model.qr.QrPayload
import kotlinx.coroutines.flow.SharedFlow
interface PairingRepository {
    val pinFlow: SharedFlow<String>
    suspend fun showPin(): Result<String>
    suspend fun getQr(): Result<QrPayload>
}
