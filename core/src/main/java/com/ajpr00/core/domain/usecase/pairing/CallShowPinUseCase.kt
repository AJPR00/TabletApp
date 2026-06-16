package com.ajpr00.core.domain.usecase.pairing

import com.ajpr00.core.domain.repository.media.TabletApiRepository
import javax.inject.Inject

class CallShowPinUseCase @Inject constructor(
    private val repo: TabletApiRepository
) {
    suspend operator fun invoke(ip: String, port: Int): Result<String> {
        return runCatching {
            val response = repo.showPin(ip, port)
                ?: error("Respuesta nula del servidor")

            if (response.status != "OK") {
                error("Estado inesperado: ${response.status}")
            }

            val salt = response.data?.get("salt")
                ?: error("Salt no recibido")

            salt
        }
    }
}
