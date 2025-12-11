package com.ajpr00.visumloop.tablet.data.datasource.local.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingDataStore by preferencesDataStore(name = "user_preferences")

@Singleton
class LoginPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "LoginPreferences"

        // Usuario local
        private val USERNAME_LOCAL = stringPreferencesKey("username_local")
        private val EMAIL_LOCAL = stringPreferencesKey("email_local")
        private val ID_TOKEN_LOCAL = stringPreferencesKey("id_token_local")
        private val AUTH_CODE_LOCAL = stringPreferencesKey("auth_code_local")

        // Usuario Google Drive
        private val EMAIL_DRIVE = stringPreferencesKey("email_drive")
        private val ID_TOKEN_DRIVE = stringPreferencesKey("id_token_drive")
        private val AUTH_CODE_DRIVE = stringPreferencesKey("auth_code_drive")

        // Configuración general
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val LANGUAGE = stringPreferencesKey("language")
    }

    // Flujos Local
    val usernameLocal: Flow<String> = context.settingDataStore.data.map {
        val value = it[USERNAME_LOCAL] ?: ""
        Log.d(TAG, "usernameLocal leído: $value")
        value
    }
    val emailLocal: Flow<String> = context.settingDataStore.data.map {
        val value = it[EMAIL_LOCAL] ?: ""
        Log.d(TAG, "emailLocal leído: $value")
        value
    }
    val idTokenLocal: Flow<String?> = context.settingDataStore.data.map {
        val value = it[ID_TOKEN_LOCAL]
        Log.d(TAG, "idTokenLocal leído: $value")
        value
    }
    val authCodeLocal: Flow<String?> = context.settingDataStore.data.map {
        val value = it[AUTH_CODE_LOCAL]
        Log.d(TAG, "authCodeLocal leído: $value")
        value
    }

    // Flujos Drive
    val emailDrive: Flow<String> = context.settingDataStore.data.map {
        val value = it[EMAIL_DRIVE] ?: ""
        Log.d(TAG, "emailDrive leído: $value")
        value
    }
    val idTokenDrive: Flow<String?> = context.settingDataStore.data.map {
        val value = it[ID_TOKEN_DRIVE]
        Log.d(TAG, "idTokenDrive leído: $value")
        value
    }
    val authCodeDrive: Flow<String?> = context.settingDataStore.data.map {
        val value = it[AUTH_CODE_DRIVE]
        Log.d(TAG, "authCodeDrive leído: $value")
        value
    }

    // Configuración
    val isDarkMode: Flow<Boolean> = context.settingDataStore.data.map {
        val value = it[IS_DARK_MODE] ?: false
        Log.d(TAG, "isDarkMode leído: $value")
        value
    }
    val language: Flow<String> = context.settingDataStore.data.map {
        val value = it[LANGUAGE] ?: "es"
        Log.d(TAG, "language leído: $value")
        value
    }

    // Métodos de guardado
    suspend fun saveLocalSession(username: String, email: String, idToken: String? = null) {
        Log.d(TAG, "Guardando sesión local: username=$username, email=$email, idToken=$idToken")
        context.settingDataStore.edit { prefs ->
            prefs[USERNAME_LOCAL] = username
            prefs[EMAIL_LOCAL] = email
            idToken?.let { prefs[ID_TOKEN_LOCAL] = it }
        }
    }

    suspend fun saveDriveSession(email: String, idToken: String? = null, authCode: String? = null) {
        Log.d(TAG, "Guardando sesión Drive: email=$email, idToken=$idToken, authCode=$authCode")
        context.settingDataStore.edit { prefs ->
            prefs[EMAIL_DRIVE] = email
            idToken?.let { prefs[ID_TOKEN_DRIVE] = it }
            authCode?.let { prefs[AUTH_CODE_DRIVE] = it }
        }
    }

    suspend fun clearLocalSession() {
        Log.d(TAG, "Limpiando sesión local")
        context.settingDataStore.edit { prefs ->
            prefs.remove(USERNAME_LOCAL)
            prefs.remove(EMAIL_LOCAL)
            prefs.remove(ID_TOKEN_LOCAL)
            prefs.remove(AUTH_CODE_LOCAL)
        }
    }

    suspend fun clearDriveSession() {
        Log.d(TAG, "Limpiando sesión Drive")
        context.settingDataStore.edit { prefs ->
            prefs.remove(EMAIL_DRIVE)
            prefs.remove(ID_TOKEN_DRIVE)
            prefs.remove(AUTH_CODE_DRIVE)
        }
    }

    suspend fun savePreferences(isDark: Boolean, lang: String) {
        Log.d(TAG, "Guardando preferencias: darkMode=$isDark, language=$lang")
        context.settingDataStore.edit { prefs ->
            prefs[IS_DARK_MODE] = isDark
            prefs[LANGUAGE] = lang
        }
    }

    suspend fun clearAll() {
        Log.d(TAG, "Limpiando todas las preferencias")
        context.settingDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
