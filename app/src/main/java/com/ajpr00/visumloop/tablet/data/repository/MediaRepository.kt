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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MediaRepository @Inject constructor(
    private val dao: MediaContentDao,
    private val ftpDataSource: FtpDataSource,
    private val driveDataSource: DataSourceGoogleDrive,
    private val loginPreferences: LoginPreferences
) {
    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    val isLocalLoggedIn: Flow<Boolean> = loginPreferences.idTokenLocal
        .map { !it.isNullOrEmpty() }

    suspend fun filterIsFavorite(list: List<MediaContent>): List<MediaContent> {
        // Obtener la lista de favoritos de la BD
        val favoritesFromDb = dao.getAllMedia().first().map { it.toDomain() }

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
    fun getAllMediaBd(): Flow<List<MediaContent>> =
        dao.getAllMedia().map { list -> list.map { it.toDomain() } }

    /*suspend fun getAllMedia(): List<MediaContent> =
        dao.getAll().map { it.toDomain() }*/

    // Inserta una media en la base de datos
    suspend fun addBd(media: MediaContent) {
        media.isFavorite = true
        dao.insert(media.toEntity())
    }

    suspend fun deleteMedia(media: MediaContent) =
        dao.delete(media.toEntity())

    /*******FTP*********/
    suspend fun listMediaFilesFTPFiltered(): List<MediaContent> {
        val files = filterRepet(ftpDataSource.listMediaFiles())
        return filterIsFavorite(files)
    }

    /*******Google Drive*********/
    suspend fun listMediaFilesDrive(accessToken: String): MediaResult {
        return try {
            Log.d("DriveFlow", "🔑 Iniciando listado de archivos en Google Drive")
            Log.d(
                "DriveFlow",
                "📌 AccessToken: $accessToken"
            )

            val files = filterRepet(driveDataSource.listDriveFiles(accessToken))

            MediaResult.Success(filterIsFavorite(files))
        } catch (e: Exception) {
            Log.e("DriveFlow", "❌ Error al cargar archivos desde Drive: ${e.localizedMessage}", e)
            MediaResult.Error("Error al cargar desde Drive", e)
        }
    }

    /*******Local*********/
    suspend fun listMediaFilesLocalFiltered(localFiles: List<MediaContent>): List<MediaContent> {
        val files = filterRepet(localFiles)
        return filterIsFavorite(files)
    }

    // Logout general (Firebase)
    fun logout() {
        Log.d("GoogleAuthRepository", "Realizando logout en Firebase")
        firebaseAuth.signOut()
    }

    // Logout específico Local
    suspend fun logoutLocal() {
        Log.d("GoogleAuthRepository", "Realizando logout local y Firebase")
        firebaseAuth.signOut()
        loginPreferences.clearLocalSession()
    }

    // Logout específico Drive
    suspend fun logoutDrive() {
        Log.d("GoogleAuthRepository", "Realizando logout Drive y Firebase")
        firebaseAuth.signOut()
        loginPreferences.clearDriveSession()
    }

    // Logout total (Firebase + DataStore completo)
    suspend fun logoutAll() {
        Log.d("GoogleAuthRepository", "Realizando logout total (Firebase + DataStore)")
        firebaseAuth.signOut()
        loginPreferences.clearAll()
    }
}
