package com.ajpr00.core.domain.usecase.media

import com.ajpr00.core.domain.excepcion.MediaException
import com.ajpr00.core.domain.model.RemoteMedia
import com.ajpr00.core.domain.repository.media.TabletApiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * UseCase encargado de obtener la lista de medios remotos junto con sus miniaturas
 * mediante un **Flow caliente** que realiza polling periódico al servidor.
 *
 * ## Rol dentro de la arquitectura
 * - Pertenece a la capa **domain**, por lo que solo contiene lógica de negocio.
 * - No conoce nada de Retrofit, HTTP ni infraestructura.
 *   Eso lo gestiona `TabletApiRepository` en la capa **data**.
 * - La UI observa este Flow para refrescar automáticamente la lista de medios.
 *
 * ## Qué resuelve
 * La API remota no es reactiva, así que este caso de uso implementa un **polling controlado**
 * para detectar cambios en la lista de medios sin saturar la red.
 *
 * ## Flujo interno del algoritmo
 * 1. Consultar la lista remota de medios.
 * 2. Comparar los IDs con la lista anterior.
 * 3. Si no hay cambios → emitir lista usando miniaturas cacheadas.
 * 4. Si hay cambios → descargar miniaturas nuevas y actualizar la cache.
 * 5. Emitir la lista final.
 * 6. Esperar X segundos y repetir.
 *
 * ## Cache interna
 * Se usa un `MutableMap<String, ByteArray>` para almacenar miniaturas ya descargadas.
 * Esto evita peticiones repetidas al servidor y mejora el rendimiento.
 *
 * ## Advertencias
 * - Si el servidor falla, se lanzan excepciones del dominio (`MediaException`).
 * - El polling debe ser razonable para no saturar la red.
 * - La UI debe cancelar el Flow cuando la pantalla se cierre.
 *
 * @property repo Repositorio remoto que gestiona las llamadas HTTP (capa data).
 */
class GetFlowRemoteMediaWithThumbnailsUseCase @Inject constructor(
    private val repo: TabletApiRepository
) {

    /** Cache en memoria para evitar descargas repetidas de miniaturas. */
    private val thumbnailCache = mutableMapOf<String, ByteArray>()

    /**
     * Inicia un flujo continuo que consulta periódicamente la lista de medios remotos.
     *
     * ## Flujo interno detallado
     * - Se consulta la lista remota.
     * - Se extraen los IDs para detectar cambios.
     * - Si la lista no cambia, se emite usando miniaturas cacheadas.
     * - Si cambia, se descargan miniaturas nuevas y se actualiza la cache.
     * - Se emite la lista final.
     * - Se espera 10 segundos antes de repetir.
     *
     * ## Manejo de excepciones
     * - Si ocurre un error de red o lectura, se lanza `MediaException.ReadError`.
     * - Si el repositorio lanza una excepción del dominio, se respeta tal cual.
     *
     * @param ip Dirección IP del servidor tablet.
     * @param port Puerto del servidor tablet.
     *
     * @return Flow que emite listas actualizadas de `RemoteMedia`.
     *
     * @throws MediaException.ReadError Si ocurre un error inesperado al obtener miniaturas.
     */
    operator fun invoke(ip: String, port: Int): Flow<List<RemoteMedia>> = flow {

        var lastIds = emptyList<String>()

        while (true) {
            try {
                println("[GetFlowRemoteMedia] Solicitando lista remota...")

                val list = repo.listMedia(ip, port)?.data?.items ?: emptyList()
                val ids = list.map { it.id }

                // Si no cambia nada → usar cache
                if (ids == lastIds) {
                    println("[GetFlowRemoteMedia] Lista sin cambios. Usando miniaturas cacheadas.")

                    emit(
                        list.map { media ->
                            RemoteMedia(
                                id = media.id,
                                name = media.name,
                                type = media.type,
                                thumbnailBytes = thumbnailCache[media.id] ?: ByteArray(0)
                            )
                        }
                    )

                    delay(10_000)
                    continue
                }

                println("[GetFlowRemoteMedia] Cambios detectados. Descargando miniaturas nuevas.")
                lastIds = ids

                val finalList = list.map { media ->
                    val thumb = thumbnailCache.getOrPut(media.id) {
                        repo.getThumbnail(ip, port, media.id)?.bytes()
                            ?: throw MediaException.ReadError
                    }

                    RemoteMedia(
                        id = media.id,
                        name = media.name,
                        type = media.type,
                        thumbnailBytes = thumb
                    )
                }

                emit(finalList)

                delay(10_000)

            }catch (e: Exception) {

                if (e is CancellationException) throw e
                if (e.cause is CancellationException) throw e

                if (e is IOException) continue
                if (e is SocketException) continue
                if (e is SocketTimeoutException) continue

                throw when (e) {
                    is MediaException -> e
                    else -> MediaException.ReadError
                }
            }

        }
    }
}