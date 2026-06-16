package com.ajpr00.core.domain.usecase.network

import com.ajpr00.core.domain.repository.network.NetworkRepository
import javax.inject.Inject

/**
 * Comprueba si el dispositivo tiene conexión real a Internet.
 *
 * ## Qué hace
 * Delegar la comprobación al `NetworkRepository`, que es quien
 * implementa la lógica real (ping, DNS, socket, etc.).
 *
 * ## Por qué existe este UseCase
 * - Evita que la capa de presentación conozca detalles de red.
 * - Permite testear la lógica sin depender de Android.
 * - Mantiene la arquitectura limpia.
 */
class CheckInternetConnectionUseCase @Inject constructor(
    private val repo: NetworkRepository
) {

    /**
     * @return `true` si hay conexión real a Internet, `false` si no.
     */
    suspend operator fun invoke(): Boolean = repo.hasInternetConnection()
}
