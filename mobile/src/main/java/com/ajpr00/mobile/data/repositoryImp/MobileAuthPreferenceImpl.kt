package com.ajpr00.mobile.data.repositoryImp

import com.ajpr00.core.domain.repository.login.AuthPreference
import com.ajpr00.mobile.data.datasource.local.preferences.SessionPreference
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MobileAuthPreferenceImpl @Inject constructor(
    private val session: SessionPreference
) : AuthPreference {

    // -------------------------
    //  SESIÓN LOCAL
    // -------------------------
    override val usernameLocal: Flow<String> = session.usernameLocal
    override val avatarLocal: Flow<String> = session.avatarLocal
    override val emailLocal: Flow<String> = session.emailLocal
    override val idTokenLocal: Flow<String?> = session.idTokenLocal
    override val authCodeLocal: Flow<String?> = session.authCodeLocal

    // -------------------------
    //  SESIÓN DRIVE
    // -------------------------
    override val emailDrive: Flow<String> = session.emailDrive
    override val idTokenDrive: Flow<String?> = session.idTokenDrive
    override val authCodeDrive: Flow<String?> = session.authCodeDrive

    // -------------------------
    //  MÉTODOS DE GUARDADO
    // -------------------------
    override suspend fun saveLocalSession(
        username: String,
        email: String,
        avatarUrl: String,
        token: String
    ) = session.saveLocalSession(username, email, avatarUrl, token)

    override suspend fun saveDriveSession(
        email: String,
        idToken: String?,
        authCode: String?
    ) = session.saveDriveSession(email, idToken, authCode)

    // -------------------------
    //  MÉTODOS DE LIMPIEZA
    // -------------------------
    override suspend fun clearLocalSession() = session.clearLocalSession()

    override suspend fun clearDriveSession() = session.clearDriveSession()

    override suspend fun clearAll() = session.clearAll()
}
