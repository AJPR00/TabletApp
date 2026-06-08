package com.ajpr00.tablet.data.repositoryImp

import android.util.Log
import com.ajpr00.core.domain.model.qr.QrPayload
import com.ajpr00.core.domain.repository.pairing.PairingRepository
import com.ajpr00.tablet.data.server.TabletServer
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class PairingRepositoryImpl @Inject constructor(
    private val server: TabletServer
) : PairingRepository {

    private val TAG = "PairingRepo"

    override val pinFlow: SharedFlow<String>
        get() {
            Log.d(TAG, "🟦 pinFlow → Exponiendo Flow del servidor (server.pinFlow)")
            return server.pinFlow
        }

    override suspend fun showPin(): Result<String> {
        Log.d(TAG, "🟦 showPin() → Llamada recibida desde ViewModel")

        return runCatching {
            Log.d(TAG, "🟦 showPin() → Delegando en server.handleShowPin()")

            val response = server.handleShowPin()

            Log.d(TAG, "🟩 showPin() → Respuesta del servidor: ${response.status} ${response.mimeType}")
            Log.d(TAG, "🟩 showPin() → El servidor debería haber emitido el PIN por pinFlow")

            "OK"
        }.onFailure { error ->
            Log.e(TAG, "🟥 showPin() → ERROR ejecutando handleShowPin(): ${error.message}", error)
        }
    }

    override suspend fun getQr(): Result<QrPayload> {
        Log.d(TAG, "getQr() → Llamada recibida desde ViewModel")
        return try {
            val response = server.implementForQRScanner()

            Log.d(TAG, "getQr() → Respuesta del servidor: ${response.status}")
            val payload = response.data
                ?: return Result.failure(Exception("QR payload es null"))
            Log.d(TAG, "getQr() → Payload del QR: $payload")

            Result.success(payload)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
