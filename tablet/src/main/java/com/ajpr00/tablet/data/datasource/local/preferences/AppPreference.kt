package com.ajpr00.tablet.data.datasource.local.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appDataStore by preferencesDataStore(name = "app_preference")

@Singleton
class AppPreference @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "AppPreference"

        // Configuración general
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val LANGUAGE = stringPreferencesKey("language")
        private val IS_FIRST_RUN = booleanPreferencesKey("is_first_run")

        // Identidad de la tablet
        private val TABLET_ID = stringPreferencesKey("tablet_id")
        private val TABLET_NAME = stringPreferencesKey("tablet_name")
    }

    // ---------------------------
    // GETTERS
    // ---------------------------

    val isDarkMode: Flow<Boolean> = context.appDataStore.data.map {
        it[IS_DARK_MODE] ?: false
    }

    val language: Flow<String> = context.appDataStore.data.map {
        it[LANGUAGE] ?: "es"
    }

    val isFirstRun: Flow<Boolean> = context.appDataStore.data.map {
        it[IS_FIRST_RUN] ?: true
    }

    val tabletId: Flow<String> = context.appDataStore.data.map {
        it[TABLET_ID] ?: ""
    }

    val tabletName: Flow<String> = context.appDataStore.data.map {
        it[TABLET_NAME] ?: "Tablet"
    }

    // ---------------------------
    // SETTERS
    // ---------------------------

    suspend fun setDarkMode(enabled: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[IS_DARK_MODE] = enabled
        }
        Log.d(TAG, "Modo oscuro actualizado: $enabled")
    }

    suspend fun setLanguage(lang: String) {
        context.appDataStore.edit { prefs ->
            prefs[LANGUAGE] = lang
        }
        Log.d(TAG, "Idioma actualizado: $lang")
    }

    suspend fun setFirstRunCompleted() {
        context.appDataStore.edit { prefs ->
            prefs[IS_FIRST_RUN] = false
        }
        Log.d(TAG, "FirstRun actualizado: false")
    }

    suspend fun setTabletId(id: String) {
        context.appDataStore.edit { prefs ->
            prefs[TABLET_ID] = id
        }
        Log.d(TAG, "TabletId asignado: $id")
    }

    suspend fun setTabletName(name: String) {
        context.appDataStore.edit { prefs ->
            prefs[TABLET_NAME] = name
        }
        Log.d(TAG, "TabletName asignado: $name")
    }
}
