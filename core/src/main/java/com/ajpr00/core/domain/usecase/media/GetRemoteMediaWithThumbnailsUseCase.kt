package com.ajpr00.core.domain.usecase.media

import com.ajpr00.core.domain.model.RemoteMedia
import com.ajpr00.core.domain.repository.media.TabletApiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * UseCase que obtiene la lista remota de medias junto con sus miniaturas
 * de forma reactiva mediante un flujo continuo (Flow).
 *
 * Este caso de uso realiza polling cada 2 segundos para consultar el estado
 * actualizado del servidor, ya que la API REST no es reactiva por sí misma.
 *
 * La UI puede observar este Flow para recibir actualizaciones automáticas
 * siempre que cambie la lista en la tablet o se envíen nuevos elementos.
 */
class GetFlowRemoteMediaWithThumbnailsUseCase @Inject constructor(
    private val tabletRepository: TabletApiRepository
) {
    operator fun invoke(ip: String, port: Int): Flow<List<RemoteMedia>> = flow {
        while (true) {
            val list = tabletRepository.listMediaWithThumbnails( ip, port)
            emit(list)
            delay(2000)
        }
    }
}

