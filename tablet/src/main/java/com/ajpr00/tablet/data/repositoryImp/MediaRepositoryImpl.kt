package com.ajpr00.tablet.data.repositoryImp

import android.util.Log
import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.core.domain.model.MediaResult
import com.ajpr00.core.domain.repository.MediaRepository
import com.ajpr00.data.datasource.cloud.DataSourceGoogleDrive
import com.ajpr00.data.datasource.cloud.FtpClientDataSource
import com.ajpr00.tablet.data.datasource.local.MediaLocalDataSource
import com.ajpr00.tablet.data.mapper.toDomain
import com.ajpr00.tablet.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    private val localDataSource: MediaLocalDataSource,
    private val ftpDataSource: FtpClientDataSource,
    private val driveDataSource: DataSourceGoogleDrive,
) : MediaRepository {

    /*************** LOCAL (ROOM) ***************/

    override fun getAllMediaBd(): Flow<List<MediaContent>> =
        localDataSource
            .getAll()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun insertAll(medias: List<MediaContent>) {
        localDataSource.insertAll(medias.map { it.toEntity() })
    }

    override suspend fun addBd(media: MediaContent) {
        localDataSource.insert(media.toEntity())
    }

    override suspend fun deleteMedia(media: MediaContent) {
        localDataSource.delete(media.toEntity())
    }

    override suspend fun getMediaById(id: String): MediaContent? {
        val entity = localDataSource.getById(id)
        return entity?.toDomain()
    }

    /*************** FAVORITOS / FILTROS ***************/

    private suspend fun filterIsFavorite(list: List<MediaContent>): List<MediaContent> {
        val favoritesFromDb = localDataSource
            .getAll()
            .first()
            .map { it.toDomain() }

        val favoritePaths = favoritesFromDb.map { it.path }.toSet()

        return list.map { media ->
            media
        }
    }

    private fun filterRepet(list: List<MediaContent>): List<MediaContent> =
        list
            .filter { it.name.isNotEmpty() && it.path.isNotEmpty() }
            .distinctBy { it.id }

    /*************** FTP ***************/

    override suspend fun listMediaFilesFTPFiltered(): List<MediaContent> {
        val files = filterRepet(ftpDataSource.listMediaFiles())
        return filterIsFavorite(files)
    }

    /*************** GOOGLE DRIVE ***************/

    override suspend fun listMediaFilesDrive(): MediaResult {
        val token = "343434" // TODO: mover a UseCase o SessionPreference

        if (token.isEmpty()) {
            return MediaResult.Error("Token no disponible")
        }

        return try {
            Log.d("DriveFlow", "Cargando archivos desde Drive...")

            val files = filterRepet(driveDataSource.listDriveFiles(token))
            MediaResult.Success(filterIsFavorite(files))

        } catch (e: Exception) {
            Log.e("DriveFlow", "Error al cargar archivos desde Drive: ${e.localizedMessage}", e)
            MediaResult.Error("Error al cargar desde Drive", e)
        }
    }

    /*************** LOCAL (LISTA YA CARGADA) ***************/

    override suspend fun listMediaFilesLocalFiltered(
        localFiles: List<MediaContent>
    ): List<MediaContent> {
        val files = filterRepet(localFiles)
        return filterIsFavorite(files)
    }

    override fun getAllSync(): List<MediaContent> =
        runBlocking {
            localDataSource.getAllOnce().map { it.toDomain() }
        }

    override fun deleteSync(id: Int) =
        runBlocking {
            localDataSource.deleteById(id)
        }

    override fun getMediaByIdSync(id: Int): MediaContent? =
        runBlocking {
            localDataSource.getById(id.toString())?.toDomain()
        }


}
