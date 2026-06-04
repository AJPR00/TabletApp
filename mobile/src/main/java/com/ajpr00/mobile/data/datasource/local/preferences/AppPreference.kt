package com.ajpr00.mobile.data.datasource.local.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.get

/**
 * AppPreference
 * -------------
 * Clase responsable de gestionar la configuración local persistente de la app
 * mediante **Jetpack DataStore (Preferences)**.
 *
 * DataStore sustituye a SharedPreferences y ofrece:
 *  - Escrituras seguras y atómicas
 *  - Acceso asíncrono mediante Flow
 *  - Evita ANRs y bloqueos de UI
 *
 * Esta clase expone:
 *  - Tema oscuro (isDarkMode)
 *  - Idioma seleccionado (language)
 *  - Flag de primera ejecución (isFirstRun)
 */

private val Context.appDataStore by preferencesDataStore(name = "app_Preference")

@Singleton
class AppPreference @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "appPreference"

        // Configuración general
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val LANGUAGE = stringPreferencesKey("language")
        private val IS_FIRST_RUN = booleanPreferencesKey("is_first_run")
    }


    // Configuración
    val isDarkMode: Flow<Boolean> = context.appDataStore.data.map {
        val value = it[IS_DARK_MODE] ?: false
        Log.d(TAG, "isDarkMode leído: $value")
        value
    }
    val language: Flow<String> = context.appDataStore.data.map {
        val value = it[LANGUAGE] ?: "es"
        Log.d(TAG, "language leído: $value")
        value
    }

    val isFirstRun: Flow<Boolean> = context.appDataStore.data.map {
        it[IS_FIRST_RUN] ?: true   // true si es primera instalacion
    }


    //Métodos de guardado configuracion app

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
}
