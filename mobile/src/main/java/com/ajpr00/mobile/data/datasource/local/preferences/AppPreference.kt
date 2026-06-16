package com.ajpr00.mobile.data.datasource.local.preferences

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
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

        // Identidad
        private val MOVIL_ID = stringPreferencesKey("movil_id")
        private val MOVIL_NAME = stringPreferencesKey("movil_name")

        // Seguridad: clave AES REAL (Base64)
        private val AES_KEY = stringPreferencesKey("aes_key")

        private val IS_CONECT_RED = booleanPreferencesKey("is_conect_red")
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

    val movilId: Flow<String> = context.appDataStore.data.map {
        val value = it[MOVIL_ID] ?: UUID.randomUUID().toString()
        Log.d(TAG, "movilId leído: $value")
        value
    }

    val movilName: Flow<String> = context.appDataStore.data.map {
        val value = it[MOVIL_NAME] ?: "NameDefinidoDS"
        Log.d(TAG, "movilName leído: $value")
        value
    }

    val isConectRed: Flow<Boolean> = context.appDataStore.data.map {
        val value = it[IS_CONECT_RED] ?: false
        Log.d(TAG, "isConectRed leído: $value")
        value
    }
    //Métodos de guardado configuracion app

    /**
     * Clave AES REAL usada para cifrar tráfico con el móvil.
     *
     * ## Detalles
     * - Se guarda en Base64.
     * - Se devuelve como `ByteArray?`.
     * - Si no existe, significa que la tablet **no está vinculada**.
     */
    val aesKey: Flow<ByteArray?> = context.appDataStore.data.map { prefs ->
        prefs[AES_KEY]?.let { base64 ->
            Base64.decode(base64, Base64.NO_WRAP)
        }
    }


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

    suspend fun setMovilId(id: String) {
        context.appDataStore.edit { prefs ->
            prefs[MOVIL_ID] = id
        }
        Log.d(TAG, "MovilId actualizado: $id")
    }

    suspend fun setMovilName(name: String) {
        context.appDataStore.edit { prefs ->
            prefs[MOVIL_NAME] = name
        }
        Log.d(TAG, "MovilName actualizado: $name")
    }


    /**
     * Guarda la clave AES.
     *
     * ## ¿Cuándo se llama?
     * - Solo después de que el móvil y la tablet hayan validado el PIN.
     * - Solo después de descifrar correctamente la clave AES REAL.
     *
     * @param bytes Clave AES en formato binario (16 bytes para AES‑128).
     */
    suspend fun setAesKey(bytes: ByteArray) {
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

        context.appDataStore.edit { prefs ->
            prefs[AES_KEY] = base64
        }
        Log.d(TAG, "Clave AES_REAL guardada correctamente (${bytes.size} bytes)")
    }

    suspend fun setMobileConnected(connected: Boolean)
    {
        context.appDataStore.edit { prefs ->
            prefs[IS_CONECT_RED] = connected
        }
        Log.d(TAG, "Estado de conexión del móvil actualizado: $connected")
    }
}
