package com.ajpr00.core.domain.usecase.network

import com.ajpr00.core.domain.repository.media.TabletApiRepository
import javax.inject.Inject

/**
 * IsTabletAliveUseCase
 *
 * Llama al repositorio para comprobar si la tablet responde al endpoint /ping.
 * Devuelve Result<String> con "pong" en caso de éxito.
 */
class IsTabletAliveUseCase @Inject constructor(
    private val repo: TabletApiRepository
) {
    suspend operator fun invoke(ip: String, port: Int): Result<Boolean> =
        runCatching {
            val response = repo.ping(ip, port)

            response.status == "OK" &&
                    response.data == "pong"
        }

}
