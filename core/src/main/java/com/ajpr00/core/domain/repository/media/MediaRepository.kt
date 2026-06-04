package com.ajpr00.core.domain.repository.media

import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.core.domain.model.MediaResult
import kotlinx.coroutines.flow.Flow

/**
 * MediaRepository
 *
 * Interfaz del dominio encargada de gestionar todos los archivos multimedia.
 * Define las operaciones para:
 *  - Leer y escribir medias en la base de datos local
 *  - Obtener listas de archivos desde distintas fuentes (FTP, Drive, local)
 *  - Consultar y eliminar medias desde el servidor interno (NanoHTTPD)
 *
 */
interface MediaRepository {
    fun getAllMediaBd(): Flow<List<MediaContent>>
    suspend fun insertAll(medias: List<MediaContent>)
    suspend fun addBd(media: MediaContent)
    suspend fun deleteMedia(media: MediaContent)
    suspend fun getMediaById(id: String): MediaContent?
    suspend fun listMediaFilesFTPFiltered(): List<MediaContent>
    suspend fun listMediaFilesDrive(): MediaResult
    suspend fun listMediaFilesLocalFiltered(localFiles: List<MediaContent>): List<MediaContent>

    // Síncrono (NanoHTTPD)
    fun getAllSync(): List<MediaContent>
    fun deleteSync(id: String)
    fun getMediaByIdSync(id: String): MediaContent?
}