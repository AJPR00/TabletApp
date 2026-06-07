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
 * DataSource local para la configuración de la app.
 *
 * ## ¿Qué guarda esta clase?
 * - Configuración general (modo oscuro, idioma).
 * - Estado de primer arranque.
 * - Identidad de la tablet (ID + nombre).
 * - **Clave AES** usada para cifrar tráfico con el móvil tras la vinculación.
 *
 * ## Responsabilidad dentro de la arquitectura
 * Esta clase pertenece a la capa **data/datasource/local** y actúa como
 * infraestructura de persistencia.
 * No contiene lógica de negocio ni validaciones complejas; solo lectura/escritura
 * de valores simples.
 *
 * ## Relación con otras capas
 * - **domain**: los casos de uso leen/escriben aquí.
 * - **presentation**: nunca accede directamente; siempre a través de domain.
 * - **core/security**: usa la clave AES REAL almacenada aquí.
 *
 * ## Notas importantes
 * - La clave AES REAL se guarda en Base64 para evitar problemas binarios.
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

        // Seguridad: clave AES REAL (Base64)
        private val AES_KEY = stringPreferencesKey("aes_key")
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    /**
     * Flujo que expone si el modo oscuro está activado.
     */
    val isDarkMode: Flow<Boolean> = context.appDataStore.data.map {
        it[IS_DARK_MODE] ?: false
    }

    /**
     * Idioma actual de la aplicación.
     */
    val language: Flow<String> = context.appDataStore.data.map {
        it[LANGUAGE] ?: "es"
    }

    /**
     * Indica si es la primera vez que se inicia la app.
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

    // -------------------------------------------------------------------------
    // SETTERS
    // -------------------------------------------------------------------------

    /**
     * Actualiza el modo oscuro.
     */
    suspend fun setDarkMode(enabled: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[IS_DARK_MODE] = enabled
        }
        Log.d(TAG, "Modo oscuro actualizado: $enabled")
    }

    /**
     * Actualiza el idioma de la aplicación.
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
}
