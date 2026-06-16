package com.ajpr00.core.domain.repository.preference

import com.ajpr00.core.domain.model.UserAuthData
import kotlinx.coroutines.flow.Flow

interface SessionManager {

    // --- Sesión local ---
    fun getLocalUsername(): Flow<String>
    fun getLocalEmail(): Flow<String>
    fun getLocalAvatar(): Flow<String>
    fun getLocalIdToken(): Flow<String?>
    fun getLocalAuthCode(): Flow<String?>
    fun getAesToken(): Flow<String>
    suspend fun saveAesToken(token: String)


    // --- Sesión Drive ---
    fun getDriveEmail(): Flow<String>
    fun getDriveIdToken(): Flow<String?>
    fun getDriveAuthCode(): Flow<String?>

    // --- Operaciones ---
    suspend fun saveLocalSession(userData: UserAuthData)

    suspend fun saveDriveSession(
        email: String,
        idToken: String? = null,
        authCode: String? = null
    )

    suspend fun clearLocalSession()
    suspend fun clearDriveSession()
    suspend fun clearAll()
}
