package com.ajpr00.visumloop.tablet.data.datasource.local.preferences

import android.content.Context
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
        private val USERNAME = stringPreferencesKey("username")
        private val EMAIL = stringPreferencesKey("email")
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val LANGUAGE = stringPreferencesKey("language")

        // Claves para Google Sign-In
        private val ID_TOKEN = stringPreferencesKey("id_token")
        private val AUTH_CODE = stringPreferencesKey("auth_code")
    }

    val username: Flow<String> = context.settingDataStore.data.map { it[USERNAME] ?: "" }
    val email: Flow<String> = context.settingDataStore.data.map { it[EMAIL] ?: "" }
    val idToken: Flow<String?> = context.settingDataStore.data.map { it[ID_TOKEN] }
    val authCode: Flow<String?> = context.settingDataStore.data.map { it[AUTH_CODE] }

    suspend fun saveUsername(username: String) {
        context.settingDataStore.edit { prefs ->
            prefs[USERNAME] = username
        }
    }

    suspend fun saveEmail(email: String) {
        context.settingDataStore.edit { prefs ->
            prefs[EMAIL] = email
        }
    }

    suspend fun saveTokens(idToken: String?, authCode: String?) {
        context.settingDataStore.edit { prefs ->
            if (idToken != null) prefs[ID_TOKEN] = idToken
            if (authCode != null) prefs[AUTH_CODE] = authCode
        }
    }

    suspend fun clearSettingDataStore() {
        context.settingDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun saveSession(username: String, email: String) {
        context.settingDataStore.edit { prefs ->
            prefs[USERNAME] = username
            prefs[EMAIL] = email
        }
    }
}
