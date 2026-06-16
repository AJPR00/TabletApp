package com.ajpr00.core.domain.usecase.pairing

import com.ajpr00.core.domain.repository.media.TabletApiRepository
import com.ajpr00.core.domain.repository.pairing.PairingRepository
import kotlinx.coroutines.flow.Flow
import sun.rmi.runtime.Log
import javax.inject.Inject

class SetParinUseCase @Inject constructor(
    private val repo: TabletApiRepository
) {
    suspend operator fun invoke(ip: String, port: Int, pin: String): Result<Pair<String, String>> {
        return runCatching {
            val response = repo.pair(ip, port, pin)
                ?: error("Respuesta nula del servidor")

            // ✔ Leer el status interno
            val innerStatus = response.data?.get("status")
                ?: error("Status interno no recibido")

            if (innerStatus != "linked") {
                error("Estado inesperado: $innerStatus")
            }

            val salt = response.data?.get("salt")
                ?: error("Salt no recibido")

            val encryptedKey = response.data?.get("encryptedAesKey")
                ?: error("encryptedAesKey no recibido")

            salt to encryptedKey
        }
    }
}
