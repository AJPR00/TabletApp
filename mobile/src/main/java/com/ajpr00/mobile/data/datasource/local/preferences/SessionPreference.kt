package com.ajpr00.mobile.data.datasource.local.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore by preferencesDataStore(name = "Session_Preference")

@Singleton
class SessionPreference @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SessionPreference"

        // Usuario local
        private val USERNAME_LOCAL = stringPreferencesKey("username_local")
        private val AVATAR_LOCAL = stringPreferencesKey("avatar_local")
        private val EMAIL_LOCAL = stringPreferencesKey("email_local")
        private val ID_TOKEN_LOCAL = stringPreferencesKey("id_token_local")
        private val AUTH_CODE_LOCAL = stringPreferencesKey("auth_code_local")

        // Usuario Google Drive
        private val EMAIL_DRIVE = stringPreferencesKey("email_drive")
        private val ID_TOKEN_DRIVE = stringPreferencesKey("id_token_drive")
        private val AUTH_CODE_DRIVE = stringPreferencesKey("auth_code_drive")

        private val AES_TOKEN = stringPreferencesKey("aes_token")

    }

    // Flujos Local

    val usernameLocal: Flow<String> = context.sessionDataStore.data.map {
        val value = it[USERNAME_LOCAL] ?: "UserLocal"
        Log.d(TAG, "usernameLocal leído: $value")
        value
    }
    val avatarLocal: Flow<String> = context.sessionDataStore.data.map {
        val value = it[AVATAR_LOCAL] ?: ""
        Log.d(TAG, "avatarLocal leído: $value")
        value
    }

    val emailLocal: Flow<String> = context.sessionDataStore.data.map {
        val value = it[EMAIL_LOCAL] ?: ""
        Log.d(TAG, "emailLocal leído: $value")
        value
    }
    val idTokenLocal: Flow<String?> = context.sessionDataStore.data.map {
        val value = it[ID_TOKEN_LOCAL]
        Log.d(TAG, "idTokenLocal leído: $value")
        value
    }
    val authCodeLocal: Flow<String?> = context.sessionDataStore.data.map {
        val value = it[AUTH_CODE_LOCAL]
        Log.d(TAG, "authCodeLocal leído: $value")
        value
    }

    val aesToken: Flow<String> = context.sessionDataStore.data.map {
        it[AES_TOKEN] ?: ""
    }

    // Flujos Drive
    val emailDrive: Flow<String> = context.sessionDataStore.data.map {
        val value = it[EMAIL_DRIVE] ?: ""
        Log.d(TAG, "emailDrive leído: $value")
        value
    }
    val idTokenDrive: Flow<String?> = context.sessionDataStore.data.map {
        val value = it[ID_TOKEN_DRIVE]
        Log.d(TAG, "idTokenDrive leído: $value")
        value
    }
    val authCodeDrive: Flow<String?> = context.sessionDataStore.data.map {
        val value = it[AUTH_CODE_DRIVE]
        Log.d(TAG, "authCodeDrive leído: $value")
        value
    }

    // Métodos de guardado
    suspend fun saveAesToken(token: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[AES_TOKEN] = token
        }
    }
    suspend fun saveLocalSession(username: String?, email: String, avatarUrl: String, token: String?) {
        context.sessionDataStore.edit { prefs ->
            prefs[USERNAME_LOCAL] = username ?: "UserLocal"
            prefs[EMAIL_LOCAL] = email
            prefs[AVATAR_LOCAL] = avatarUrl
            prefs[ID_TOKEN_LOCAL] = token ?: ""
        }
        Log.d(TAG, "Guardando sesión local, email: $email, token: ${token?.take(10)}..., avatar: $avatarUrl, username: $username")
    }

    suspend fun saveDriveSession(email: String, idToken: String? = null, authCode: String? = null) {
        context.sessionDataStore.edit { prefs ->
            prefs[EMAIL_DRIVE] = email
            idToken?.let { prefs[ID_TOKEN_DRIVE] = it }
            authCode?.let { prefs[AUTH_CODE_DRIVE] = it }
        }
        Log.d(TAG, "Guardando sesión Drive: email=$email, idToken=$idToken, authCode=$authCode")
    }

    suspend fun clearLocalSession() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(USERNAME_LOCAL)
            prefs.remove(EMAIL_LOCAL)
            prefs.remove(ID_TOKEN_LOCAL)
            prefs.remove(AUTH_CODE_LOCAL)
        }
        Log.d(TAG, "Limpiando sesión local")
    }

    suspend fun clearDriveSession() {
        Log.d(TAG, "Limpiando sesión Drive")
        context.sessionDataStore.edit { prefs ->
            prefs.remove(EMAIL_DRIVE)
            prefs.remove(ID_TOKEN_DRIVE)
            prefs.remove(AUTH_CODE_DRIVE)
        }
    }

    suspend fun clearAll() {
        Log.d(TAG, "Limpiando todas las preferencias")
        context.sessionDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
