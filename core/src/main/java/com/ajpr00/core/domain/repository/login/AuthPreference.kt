package com.ajpr00.core.domain.repository.login

import kotlinx.coroutines.flow.Flow

interface AuthPreference {

    // -------------------------
    //  SESIÓN LOCAL
    // -------------------------
    val usernameLocal: Flow<String>
    val avatarLocal: Flow<String>
    val emailLocal: Flow<String>
    val idTokenLocal: Flow<String?>
    val authCodeLocal: Flow<String?>

    // -------------------------
    //  SESIÓN DRIVE
    // -------------------------
    val emailDrive: Flow<String>
    val idTokenDrive: Flow<String?>
    val authCodeDrive: Flow<String?>

    // -------------------------
    //  MÉTODOS DE GUARDADO
    // -------------------------
    suspend fun saveLocalSession(
        username: String,
        email: String,
        avatarUrl: String,
        token: String
    )

    suspend fun saveDriveSession(
        email: String,
        idToken: String? = null,
        authCode: String? = null
    )

    // -------------------------
    //  MÉTODOS DE LIMPIEZA
    // -------------------------
    suspend fun clearLocalSession()
    suspend fun clearDriveSession()
    suspend fun clearAll()
}
