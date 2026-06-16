package com.ajpr00.tablet.data.repositoryImp.preference

import com.ajpr00.core.domain.repository.preference.SettingsManager
import com.ajpr00.tablet.data.datasource.local.preferences.AppPreference
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementación del repositorio de preferencias generales de la **tablet**.
 *
 * ## Rol dentro de la arquitectura
 * Esta clase pertenece a la capa **data**, por lo que:
 * - Actúa como puente entre **domain** y el almacenamiento real (DataStore).
 * - Implementa la interfaz `AppSettingsRepository` definida en **core/domain**.
 * - No contiene lógica de negocio; solo delega operaciones a `AppPreference`.
 *
 * ## Qué gestiona este repositorio
 * Maneja configuraciones persistentes de la tablet, como:
 *
 * ### Preferencias de UI
 * - Modo oscuro.
 * - Idioma.
 * - Estado de primera ejecución.
 *
 * ### Identidad de la tablet
 * - ID único.
 * - Nombre asignado por el usuario.
 *
 * ### Estado de conexión con el móvil
 * - Si hay un móvil emparejado.
 * - Nombre del móvil conectado.
 *
 * ### Seguridad y cifrado
 * - Token de emparejamiento.
 * - Clave AES para cifrado de archivos.
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
class TabletAppPreferencesRepositoryImpl @Inject constructor(
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
     * Devuelve el idioma actual configurado en la tablet.
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
     * Devuelve el ID único de la tablet.
     *
     * @return Flow<String> con el ID.
     */
    override fun getDeviceId(): Flow<String> = prefs.tabletId

    /**
     * Devuelve el nombre asignado a la tablet.
     *
     * @return Flow<String> con el nombre.
     */
    override fun getDeviceName(): Flow<String> = prefs.tabletName

    /**
     * Indica si un móvil está emparejado con la tablet.
     *
     * @return Flow<Boolean> reactivo.
     */
    override fun isDeviceConnected(): Flow<Boolean> = prefs.isMobileConnected

    /**
     * Devuelve el token de emparejamiento móvil–tablet.
     *
     * @return Flow<String> con el token.
     */
    override fun getToken(): Flow<String> = prefs.token

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
        println("[TabletAppPrefs] Guardando modo oscuro: $enabled")
        prefs.setDarkMode(enabled)
    }

    /**
     * Guarda el idioma seleccionado por el usuario.
     *
     * @param lang Código del idioma.
     */
    override suspend fun setLanguage(lang: String) {
        println("[TabletAppPrefs] Guardando idioma: $lang")
        prefs.setLanguage(lang)
    }

    /**
     * Marca que la primera ejecución ya ha sido completada.
     */
    override suspend fun setFirstRunCompleted() {
        println("[TabletAppPrefs] Marcando primera ejecución como completada")
        prefs.setFirstRunCompleted()
    }

    /**
     * Guarda el ID único de la tablet.
     *
     * @param id Identificador único generado por la app.
     */
    override suspend fun setDeviceId(id: String) {
        println("[TabletAppPrefs] Guardando ID de la tablet: $id")
        prefs.setTabletId(id)
    }

    /**
     * Guarda el nombre de la tablet.
     *
     * @param name Nombre asignado por el usuario.
     */
    override suspend fun setDeviceName(name: String) {
        println("[TabletAppPrefs] Guardando nombre de la tablet: $name")
        prefs.setTabletName(name)
    }

    /**
     * Guarda el estado de conexión con el móvil.
     *
     * @param connected `true` si el móvil está emparejado.
     */
    override suspend fun setDeviceConnected(connected: Boolean) {
        println("[TabletAppPrefs] Guardando estado de conexión móvil: $connected")
        prefs.setMobileConnected(connected)
    }

    /**
     * Guarda el token de emparejamiento móvil–tablet.
     *
     * @param token Token generado durante el emparejamiento.
     */
    override suspend fun setToken(token: String) {
        println("[TabletAppPrefs] Guardando token de emparejamiento")
        prefs.setToken(token)
    }

    /**
     * Guarda la clave AES generada durante el emparejamiento móvil–tablet.
     *
     * @param bytes Clave AES en formato binario.
     */
    override suspend fun setAesKey(bytes: ByteArray) {
        println("[TabletAppPrefs] Guardando clave AES (${bytes.size} bytes)")
        prefs.setAesKey(bytes)
    }
}