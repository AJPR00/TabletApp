package com.ajpr00.tablet.data.datasource.local.preferences

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appDataStore by preferencesDataStore(name = "app_preference")

/**
 * # AppPreference
 *
 * DataSource local basado en **Jetpack DataStore Preferences** encargado de
 * persistir la configuración interna de la tablet.
 *
 * ## ¿Qué guarda esta clase?
 * - Configuración general (modo oscuro, idioma).
 * - Estado de primer arranque.
 * - Identidad de la tablet (ID + nombre).
 * - Estado de conexión con el móvil.
 * - Token de sesión local.
 * - **Clave AES REAL** usada para cifrar tráfico con el móvil tras el pairing.
 *
 * ## Rol dentro de la arquitectura
 * - Pertenece a la capa **data/datasource/local**.
 * - Es infraestructura pura: solo lectura/escritura de valores simples.
 * - No contiene lógica de negocio ni validaciones complejas.
 *
 * ## Relación con otras capas
 * - **domain**: los casos de uso leen/escriben aquí.
 * - **presentation**: nunca accede directamente; siempre a través de domain.
 * - **core/security**: usa la clave AES REAL almacenada aquí.
 *
 * ## Notas importantes
 * - La clave AES REAL se guarda en Base64 para evitar problemas binarios.
 * - Ningún valor aquí debe bloquear el hilo principal.
 */
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

        // Token de sesión
        private val TOKEN = stringPreferencesKey("token")

        // Estado de conexión con el móvil
        private val IS_MOBILE_CONNECTED = booleanPreferencesKey("is_mobile_connected")
        private val MOBILE_NAME = stringPreferencesKey("mobile_name")

        // Seguridad: clave AES REAL (Base64)
        private val AES_KEY = stringPreferencesKey("aes_key")
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    /**
     * Flujo que expone si el modo oscuro está activado.
     *
     * ## Notas
     * - Devuelve `false` por defecto.
     */
    val isDarkMode: Flow<Boolean> = context.appDataStore.data.map {
        it[IS_DARK_MODE] ?: false
    }

    /**
     * Idioma actual de la aplicación.
     *
     * ## Notas
     * - Devuelve `"es"` por defecto.
     */
    val language: Flow<String> = context.appDataStore.data.map {
        it[LANGUAGE] ?: "es"
    }

    /**
     * Indica si es la primera vez que se inicia la app.
     *
     * ## Notas
     * - Devuelve `true` por defecto.
     */
    val isFirstRun: Flow<Boolean> = context.appDataStore.data.map {
        it[IS_FIRST_RUN] ?: true
    }

    /**
     * Identificador único de la tablet generado en el onboarding.
     */
    val tabletId: Flow<String> = context.appDataStore.data.map {
        it[TABLET_ID] ?: ""
    }

    /**
     * Nombre asignado por el usuario a la tablet.
     */
    val tabletName: Flow<String> = context.appDataStore.data.map {
        it[TABLET_NAME] ?: "Tablet"
    }

    /**
     * Indica si el móvil está actualmente vinculado y conectado.
     */
    val isMobileConnected: Flow<Boolean> = context.appDataStore.data.map {
        it[IS_MOBILE_CONNECTED] ?: false
    }

    /**
     * Nombre del móvil vinculado.
     */
    val mobileName: Flow<String> = context.appDataStore.data.map {
        it[MOBILE_NAME] ?: "Movil"
    }

    /**
     * Token de sesión local generado tras el pairing.
     */
    val token: Flow<String> = context.appDataStore.data.map {
        it[TOKEN] ?: ""
    }

    /**
     * Clave AES REAL usada para cifrar tráfico con el móvil.
     *
     * ## Detalles técnicos
     * - Se guarda en Base64.
     * - Se devuelve como `ByteArray?`.
     * - Si no existe, significa que la tablet **no está vinculada**.
     */
    val aesKey: Flow<ByteArray?> = context.appDataStore.data.map { prefs ->
        prefs[AES_KEY]?.let { base64 ->
            Base64.decode(base64, Base64.NO_WRAP)
        }
    }

    // -------------------------------------------------------------------------
    // SETTERS
    // -------------------------------------------------------------------------

    /**
     * Actualiza el modo oscuro.
     *
     * @param enabled `true` para activar, `false` para desactivar.
     */
    suspend fun setDarkMode(enabled: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[IS_DARK_MODE] = enabled
        }
        Log.d(TAG, "Modo oscuro actualizado: $enabled")
    }

    /**
     * Actualiza el idioma de la aplicación.
     *
     * @param lang Código ISO del idioma (ej: `"es"`, `"en"`).
     */
    suspend fun setLanguage(lang: String) {
        context.appDataStore.edit { prefs ->
            prefs[LANGUAGE] = lang
        }
        Log.d(TAG, "Idioma actualizado: $lang")
    }

    /**
     * Marca que el onboarding ha sido completado.
     */
    suspend fun setFirstRunCompleted() {
        context.appDataStore.edit { prefs ->
            prefs[IS_FIRST_RUN] = false
        }
        Log.d(TAG, "Primer arranque marcado como completado")
    }

    /**
     * Guarda el identificador único de la tablet.
     */
    suspend fun setTabletId(id: String) {
        context.appDataStore.edit { prefs ->
            prefs[TABLET_ID] = id
        }
        Log.d(TAG, "TabletId asignado: $id")
    }

    /**
     * Guarda el nombre de la tablet.
     */
    suspend fun setTabletName(name: String) {
        context.appDataStore.edit { prefs ->
            prefs[TABLET_NAME] = name
        }
        Log.d(TAG, "TabletName asignado: $name")
    }

    /**
     * Guarda la clave AES REAL.
     *
     * ## ¿Cuándo se llama?
     * - Después de validar el PIN en `/pair`.
     * - Después de descifrar correctamente la clave AES REAL enviada por el móvil.
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

    /**
     * Actualiza el estado de conexión con el móvil.
     */
    suspend fun setMobileConnected(connected: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[IS_MOBILE_CONNECTED] = connected
        }
        Log.d(TAG, "MobileConnected actualizado: $connected")
    }

    /**
     * Guarda el nombre del móvil vinculado.
     */
    suspend fun setMobileName(name: String) {
        context.appDataStore.edit { prefs ->
            prefs[MOBILE_NAME] = name
        }
        Log.d(TAG, "MobileName asignado: $name")
    }

    /**
     * Guarda el token de sesión generado tras el pairing.
     */
    suspend fun setToken(name: String) {
        context.appDataStore.edit { prefs ->
            prefs[TOKEN] = name
        }
        Log.d(TAG, "Token asignado: $name")
    }
}