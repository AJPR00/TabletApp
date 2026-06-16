package com.ajpr00.core.domain.repository.preference

import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de preferencias generales del dispositivo (móvil o tablet).
 *
 * ## Rol dentro de la arquitectura
 * Esta interfaz pertenece a la capa **domain**, por lo que:
 * - Define una API limpia y estable para acceder a las preferencias del dispositivo.
 * - No conoce DataStore, Android ni detalles de infraestructura.
 * - Es implementada por la capa **data**, que se encarga del almacenamiento real.
 *
 * ## Qué representa este repositorio
 * Un conjunto de configuraciones persistentes que afectan al comportamiento global
 * de la aplicación, como:
 *
 * ### Preferencias de UI
 * - Modo oscuro.
 * - Idioma.
 * - Estado de primera ejecución.
 *
 * ### Identidad del dispositivo
 * - ID único.
 * - Nombre asignado por el usuario.
 *
 * ### Estado de conexión
 * - Si el dispositivo está emparejado o conectado.
 *
 * ### Seguridad
 * - Token de emparejamiento.
 * - Clave AES para cifrado.
 *
 * ## Por qué existe esta interfaz
 * - Permite desacoplar la lógica de negocio del almacenamiento real.
 * - Facilita testing (pueden crearse implementaciones fake o in-memory).
 * - Evita dependencias directas de domain hacia Android.
 *
 * ## Notas importantes
 * - Todas las lecturas se exponen como `Flow`, permitiendo a la UI reaccionar a cambios.
 * - Las escrituras son `suspend` porque DataStore es asíncrono.
 */
interface SettingsManager {

    // ---------------------------------------------------------
    // CONFIGURACIÓN GENERAL
    // ---------------------------------------------------------

    /**
     * Indica si el modo oscuro está activado.
     *
     * @return Flow<Boolean> reactivo.
     */
    fun isDarkMode(): Flow<Boolean>

    /**
     * Devuelve el idioma actual configurado en el dispositivo.
     *
     * @return Flow<String> con el código del idioma.
     */
    fun getLanguage(): Flow<String>

    /**
     * Indica si es la primera vez que se ejecuta la app.
     *
     * @return Flow<Boolean> reactivo.
     */
    fun isFirstRun(): Flow<Boolean>

    // ---------------------------------------------------------
    // IDENTIDAD DEL DISPOSITIVO
    // ---------------------------------------------------------

    /**
     * Devuelve el ID único del dispositivo.
     *
     * @return Flow<String> con el ID.
     */
    fun getDeviceId(): Flow<String>

    /**
     * Devuelve el nombre asignado al dispositivo.
     *
     * @return Flow<String> con el nombre.
     */
    fun getDeviceName(): Flow<String>

    // ---------------------------------------------------------
    // ESTADO DE CONEXIÓN
    // ---------------------------------------------------------

    /**
     * Indica si el dispositivo está conectado o emparejado.
     *
     * @return Flow<Boolean> reactivo.
     */
    fun isDeviceConnected(): Flow<Boolean>

    // ---------------------------------------------------------
    // TOKEN
    // ---------------------------------------------------------

    /**
     * Devuelve el token de emparejamiento o autenticación.
     *
     * @return Flow<String> con el token.
     */
    fun getToken(): Flow<String>

    // ---------------------------------------------------------
    // SEGURIDAD
    // ---------------------------------------------------------

    /**
     * Devuelve la clave AES almacenada localmente.
     *
     * @return Flow<ByteArray?> con la clave o null si no existe.
     */
    fun getAesKey(): Flow<ByteArray?>

    // ---------------------------------------------------------
    // SETTERS
    // ---------------------------------------------------------

    /**
     * Guarda el estado del modo oscuro.
     *
     * @param enabled `true` para activar modo oscuro.
     */
    suspend fun setDarkMode(enabled: Boolean)

    /**
     * Guarda el idioma seleccionado por el usuario.
     *
     * @param lang Código del idioma.
     */
    suspend fun setLanguage(lang: String)

    /**
     * Marca que la primera ejecución ya ha sido completada.
     */
    suspend fun setFirstRunCompleted()

    /**
     * Guarda el ID único del dispositivo.
     *
     * @param id Identificador único generado por la app.
     */
    suspend fun setDeviceId(id: String)

    /**
     * Guarda el nombre del dispositivo.
     *
     * @param name Nombre asignado por el usuario.
     */
    suspend fun setDeviceName(name: String)

    /**
     * Guarda el estado de conexión del dispositivo.
     *
     * @param connected `true` si está conectado.
     */
    suspend fun setDeviceConnected(connected: Boolean)

    /**
     * Guarda el token de emparejamiento o autenticación.
     *
     * @param token Token generado por la app o el servidor.
     */
    suspend fun setToken(token: String)

    /**
     * Guarda la clave AES generada durante el emparejamiento.
     *
     * @param bytes Clave AES en formato binario.
     */
    suspend fun setAesKey(bytes: ByteArray)
}