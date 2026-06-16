package com.ajpr00.data.useCase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ajpr00.core.domain.excepcion.MediaException
import com.ajpr00.data.exception.DatabaseException
import com.ajpr00.core.domain.repository.media.PendingMediaRepository
import com.ajpr00.data.mapper.tablet.toPendingMedia
import javax.inject.Inject

/**
 * ## AddPendingMediaUseCase
 *
 * UseCase encargado de **importar archivos seleccionados por el usuario**
 * (imágenes o vídeos) y convertirlos en `PendingMedia` para añadirlos a la cola
 * de envío hacia la tablet.
 *
 * ### Responsabilidad dentro de la arquitectura
 * - Pertenece a la **Domain layer**.
 * - No toca infraestructura directamente (FileManager, Room, etc.).
 * - Orquesta:
 *   1. Conversión de `Uri` → `PendingMedia`.
 *   2. Asignación del `deviceId`.
 *   3. Inserción en el repositorio.
 *
 * ### Flujo interno
 * 1. Itera cada `Uri` seleccionada.
 * 2. Convierte la `Uri` en `PendingMedia` usando el mapper.
 * 3. Asigna el `deviceId` del dispositivo seleccionado.
 * 4. Inserta cada elemento en la BD.
 *
 * ### Parámetros
 * @param uris Lista de URIs seleccionadas por el usuario.
 * @param id Identificador del dispositivo al que pertenecen los archivos.
 * @param context Context necesario para resolver MIME y copiar archivos.
 *
 * ### Excepciones
 * - `MediaException.ReadError` si falla la conversión desde `Uri`.
 * - `DatabaseException.WriteError` si falla la inserción en BD.
 */
class AddPendingMediaUseCase @Inject constructor(
    private val repository: PendingMediaRepository,
) {

    suspend operator fun invoke(
        uris: List<Uri>,
        id: String,
        context: Context
    ) {
        try {
            uris.forEach { uri ->

                val pendingMedia = try {
                    uri.toPendingMedia(context)
                } catch (e: Exception) {
                    Log.e("AddPendingMediaUC", "Error convirtiendo URI: ${e.message}")
                    throw MediaException.ReadError
                }

                pendingMedia.deviceId = id

                try {
                    repository.insert(pendingMedia)
                } catch (e: Exception) {
                    Log.e("AddPendingMediaUC", "Error insertando en BD: ${e.message}")
                    throw DatabaseException.WriteError
                }
            }

        } catch (e: MediaException) {
            Log.e("AddPendingMediaUC", "Error de media: ${e.code}")
            throw e

        } catch (e: DatabaseException) {
            Log.e("AddPendingMediaUC", "Error de BD: ${e.code}")
            throw e

        } catch (e: Exception) {
            Log.e("AddPendingMediaUC", "Error inesperado: ${e.message}")
            throw MediaException.ReadError
        }
    }
}
