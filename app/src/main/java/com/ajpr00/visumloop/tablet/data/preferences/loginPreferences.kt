package com.ajpr00.visumloop.tablet.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingDataStore by preferencesDataStore(name = "user_preferences")

class loginPreferences(private val context: Context) {
    companion object {
        private val USERNAME = stringPreferencesKey("username")
        private val EMAIL = stringPreferencesKey("email")

        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val LANGUAGE = stringPreferencesKey("language")

    }

    val username: Flow<String> = context.settingDataStore.data.map { it[USERNAME] ?: "" }
    val email: Flow<String> = context.settingDataStore.data.map { it[EMAIL] ?: "" }

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
