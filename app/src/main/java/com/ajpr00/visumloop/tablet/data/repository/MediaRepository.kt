package com.ajpr00.visumloop.tablet.data.repository

import android.util.Log
import com.ajpr00.visumloop.tablet.data.datasource.cloud.DataSourceGoogleDrive
import com.ajpr00.visumloop.tablet.data.datasource.cloud.FtpDataSource
import com.ajpr00.visumloop.tablet.data.datasource.local.db.dao.MediaContentDao
import com.ajpr00.visumloop.tablet.data.datasource.local.preferences.LoginPreferences
import com.ajpr00.visumloop.tablet.data.mapper.toDomain
import com.ajpr00.visumloop.tablet.data.mapper.toEntity
import com.ajpr00.visumloop.tablet.domain.model.MediaContent
import com.ajpr00.visumloop.tablet.domain.model.MediaResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MediaRepository @Inject constructor(
    private val dao: MediaContentDao,
    private val ftpDataSource: FtpDataSource,
    private val driveDataSource: DataSourceGoogleDrive,
    private val loginPreferences: LoginPreferences
) {
    /*******ROOM*********/
    // Obtiene todos los media de la base de datos
    fun getAllMediaBd(): Flow<List<MediaContent>> =
        dao.getAllMedia().map { list -> list.map { it.toDomain() } }

    // Obtiene todas las medias de la base de datos
    /*suspend fun getAllMedia(): List<MediaContent> =
        dao.getAll().map { it.toDomain() }*/

    // Inserta una media en la base de datos
    suspend fun insertMedia(media: MediaContent) =
        dao.insert(media.toEntity())

    suspend fun deleteMedia(media: MediaContent) =
        dao.delete(media.toEntity())

    /*******FTP*********/
    suspend fun listMediaFilesFTP(): List<MediaContent> =
        ftpDataSource.listMediaFiles()

    /*******Google Drive*********/
    suspend fun listMediaFilesDrive(accessToken: String): MediaResult {
        return try {
            Log.d("DriveFlow", "🔑 Iniciando listado de archivos en Google Drive")
            Log.d("DriveFlow", "📌 AccessToken: $accessToken") // Solo para depuración; no mostrar en producción

            val files = driveDataSource.listDriveFiles(accessToken)

            Log.d("DriveFlow", "🟢 Archivos obtenidos de Drive:")
            files.forEach { file ->
                Log.d("DriveFlow", "    • ${file.name} (ID: ${file.id}, MIME: ${file.type})")
            }

            MediaResult.Success(files)
        } catch (e: Exception) {
            Log.e("DriveFlow", "❌ Error al cargar archivos desde Drive: ${e.localizedMessage}", e)
            MediaResult.Error("Error al cargar desde Drive", e)
        }
    }



    /*******Local*********/
}
