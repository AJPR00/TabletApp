package com.ajpr00.visumloop.tablet.data.repository.impl

import android.util.Log
import com.ajpr00.visumloop.tablet.data.datasource.cloud.DataSourceGoogleDrive
import com.ajpr00.visumloop.tablet.data.datasource.cloud.FtpClientDataSource
import com.ajpr00.visumloop.tablet.data.datasource.local.db.dao.MediaContentDao
import com.ajpr00.visumloop.tablet.data.mapper.toDomain
import com.ajpr00.visumloop.tablet.data.mapper.toEntity
import com.ajpr00.visumloop.tablet.data.repository.MediaRepository
import com.ajpr00.visumloop.tablet.domain.model.MediaContent
import com.ajpr00.visumloop.tablet.domain.model.MediaResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    private val daoBD: MediaContentDao,
    private val ftpDataSource: FtpClientDataSource,
    private val driveDataSource: DataSourceGoogleDrive,
): MediaRepository {

    suspend fun filterIsFavorite(list: List<MediaContent>): List<MediaContent> {
        // Obtener la lista de favoritos de la BD
        val favoritesFromDb = daoBD.getAllMedia().first().map { it.toDomain() }

        // Crear un conjunto de paths favoritos para búsqueda rápida
        val favoritePaths = favoritesFromDb.map { it.path }.toSet()

        // Marcar como favorito los que están en la BD
        return list.map { media ->
            if (media.path in favoritePaths) media.copy(isFavorite = true)
            else media
        }
    }

    fun filterRepet(list: List<MediaContent>): List<MediaContent> {
        return list.filter { it.name.isNotEmpty() && it.path.isNotEmpty() }
            .distinctBy { it.id }
    }

    /*******ROOM*********/

    // Obtiene todos los media de la base de datos
    override fun getAllMediaBd(): Flow<List<MediaContent>> =
        daoBD.getAllMedia().map { list -> list.map { it.toDomain() } }

    /*suspend fun getAllMedia(): List<MediaContent> =
        dao.getAll().map { it.toDomain() }*/

    // Inserta una media en la base de datos
    override suspend fun addBd(media: MediaContent) {
        media.isFavorite = true
        daoBD.insert(media.toEntity())
    }

    override suspend fun deleteMedia(media: MediaContent) =
        daoBD.delete(media.toEntity())

    /*******FTP*********/
    override suspend fun listMediaFilesFTPFiltered(): List<MediaContent> {
        val files = filterRepet(ftpDataSource.listMediaFiles())
        return filterIsFavorite(files)
    }

    /*******Google Drive*********/
    override suspend fun listMediaFilesDrive(): MediaResult {
        val token = "343434" //Todo Funcion fuera de lugar "Quitar"

        if (token.isNullOrEmpty()) {
            return MediaResult.Error("Token no disponible")
        }

        return try {
            Log.d("DriveFlow", "✅ Cargando archivos desde Drive...")
            Log.d("DriveFlow", "Token: $token")
            val files = filterRepet(driveDataSource.listDriveFiles(token))
            MediaResult.Success(filterIsFavorite(files))
        } catch (e: Exception) {
            Log.e("DriveFlow", "❌ Error al cargar archivos desde Drive: ${e.localizedMessage}", e)
            MediaResult.Error("Error al cargar desde Drive", e)
        }
    }

    /*******Local*********/
    override suspend fun listMediaFilesLocalFiltered(localFiles: List<MediaContent>): List<MediaContent> {
        val files = filterRepet(localFiles)
        return filterIsFavorite(files)
    }
}
