package com.ajpr00.mobile.data.repositoryImp.prefrence

import android.util.Log
import com.ajpr00.core.domain.repository.preference.SettingsManager
import com.ajpr00.mobile.data.datasource.local.preferences.AppPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Implementación del repositorio de preferencias de la app móvil.
 *
 * ## Rol dentro de la arquitectura
 * Esta clase pertenece a la capa **data**, por lo que:
 * - Actúa como puente entre **domain** y el almacenamiento real (DataStore).
 * - Implementa la interfaz `AppSettingsRepository` definida en **core/domain**.
 * - No contiene lógica de negocio; solo delega operaciones a `AppPreference`.
 *
 * ## Qué gestiona este repositorio
 * Maneja configuraciones persistentes del móvil, como:
 * - Modo oscuro.
 * - Idioma.
 * - Estado de primera ejecución.
 * - Identidad del dispositivo móvil (ID + nombre).
 * - Estado de conexión del móvil.
 * - Clave AES para cifrado.
 *
 * ## Por qué existe esta capa
 * Domain no debe conocer DataStore ni Android.
 * Este repositorio encapsula esa infraestructura y expone una API limpia.
 *
 * ## Notas importantes
 * - Todas las lecturas se exponen como `Flow`, permitiendo a la UI reaccionar a cambios.
 * - Las escrituras son `suspend` porque DataStore es asíncrono.
 * - No se lanzan excepciones del dominio aquí; DataStore ya maneja sus propios errores.
 */
class MobileAppPreferencesRepositoryImpl @Inject constructor(
    private val prefs: AppPreference
) : SettingsManager {

    // ---------------------------------------------------------
    // GETTERS
    // ---------------------------------------------------------

    /**
     * Indica si el modo oscuro está activado.
     *
     * @return Flow<Boolean> reactivo.
     */
    override fun isDarkMode(): Flow<Boolean> = prefs.isDarkMode

    /**
     * Devuelve el idioma actual configurado en el móvil.
     *
     * @return Flow<String> con el código del idioma.
     */
    override fun getLanguage(): Flow<String> = prefs.language

    /**
     * Indica si es la primera vez que se ejecuta la app.
     *
     * @return Flow<Boolean> reactivo.
     */
    override fun isFirstRun(): Flow<Boolean> = prefs.isFirstRun

    /**
     * Devuelve el ID único del móvil.
     *
     * @return Flow<String> con el ID.
     */
    override fun getDeviceId(): Flow<String> = prefs.movilId

    /**
     * Devuelve el nombre asignado al móvil.
     *
     * @return Flow<String> con el nombre.
     */
    override fun getDeviceName(): Flow<String> = prefs.movilName

    /**
     * Indica si el móvil está conectado a la red o emparejado.
     *
     * @return Flow<Boolean> reactivo.
     */
    override fun isDeviceConnected(): Flow<Boolean> = prefs.isConectRed

    /**
     * Devuelve el token de emparejamiento.
     *
     * ## Nota
     * El móvil no usa token, así que se devuelve un Flow vacío.
     */
    override fun getToken(): Flow<String> = flowOf("")

    /**
     * Devuelve la clave AES almacenada localmente.
     *
     * @return Flow<ByteArray?> con la clave o null si no existe.
     */
    override fun getAesKey(): Flow<ByteArray?> = prefs.aesKey

    // ---------------------------------------------------------
    // SETTERS
    // ---------------------------------------------------------

    /**
     * Guarda el estado del modo oscuro.
     *
     * @param enabled `true` para activar modo oscuro.
     */
    override suspend fun setDarkMode(enabled: Boolean) {
        Log.d("MobilePrefs", "Guardando modo oscuro: $enabled")
        prefs.setDarkMode(enabled)
    }

    /**
     * Guarda el idioma seleccionado por el usuario.
     *
     * @param lang Código del idioma.
     */
    override suspend fun setLanguage(lang: String) {
        Log.d("MobilePrefs", "Guardando idioma: $lang")
        prefs.setLanguage(lang)
    }

    /**
     * Marca que la primera ejecución ya ha sido completada.
     */
    override suspend fun setFirstRunCompleted() {
        Log.d("MobilePrefs", "Marcando primera ejecución como completada")
        prefs.setFirstRunCompleted()
    }

    /**
     * Guarda el ID único del móvil.
     *
     * @param id Identificador único generado por la app.
     */
    override suspend fun setDeviceId(id: String) {
        Log.d("MobilePrefs", "Guardando ID del móvil: $id")
        prefs.setMovilId(id)
    }

    /**
     * Guarda el nombre del móvil.
     *
     * @param name Nombre asignado por el usuario.
     */
    override suspend fun setDeviceName(name: String) {
        Log.d("MobilePrefs", "Guardando nombre del móvil: $name")
        prefs.setMovilName(name)
    }

    /**
     * Guarda el estado de conexión del móvil.
     *
     * @param connected `true` si el móvil está conectado.
     */
    override suspend fun setDeviceConnected(connected: Boolean) {
        Log.d("MobilePrefs", "Guardando estado de conexión: $connected")
        prefs.setMobileConnected(connected)
    }

    /**
     * Guarda el token de emparejamiento.
     *
     * ## Nota
     * El móvil no usa token, así que esta operación es no-op.
     */
    override suspend fun setToken(token: String) {
        Log.d("MobilePrefs", "setToken() ignorado: el móvil no usa token")
    }

    /**
     * Guarda la clave AES generada durante el emparejamiento móvil–tablet.
     *
     * @param bytes Clave AES en formato binario.
     */
    override suspend fun setAesKey(bytes: ByteArray) {
        Log.d("MobilePrefs", "Guardando clave AES (${bytes.size} bytes)")
        prefs.setAesKey(bytes)
    }
}