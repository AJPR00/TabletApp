package com.ajpr00.tablet.data.repositoryImp.preference

import com.ajpr00.core.domain.model.UserAuthData
import com.ajpr00.core.domain.repository.preference.SessionManager
import com.ajpr00.tablet.data.datasource.local.preferences.SessionPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Implementación del repositorio de sesión para la **tablet**.
 *
 * ## Rol dentro de la arquitectura
 * Esta clase pertenece a la capa **data**, por lo que:
 * - Actúa como puente entre **domain** y el almacenamiento real (DataStore).
 * - Implementa la interfaz `SessionRepository` definida en **core/domain**.
 * - No contiene lógica de negocio; solo delega operaciones a `SessionPreference`.
 *
 * ## Qué gestiona este repositorio
 * Maneja datos temporales de sesión del usuario en la tablet:
 *
 * ### Sesión local (Firebase / autenticación interna)
 * - Nombre de usuario.
 * - Email.
 * - Avatar.
 * - ID Token.
 * - Auth Code.
 *
 * ### Sesión de Google Drive
 * - Email.
 * - ID Token.
 * - Auth Code.
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
class TabletSessionPreferencesRepositoryImpl @Inject constructor(
    private val prefs: SessionPreference
) : SessionManager {

    // ---------------------------------------------------------
    // LOCAL SESSION (Firebase / autenticación interna)
    // ---------------------------------------------------------

    /**
     * Devuelve el nombre de usuario almacenado en la sesión local.
     *
     * @return Flow<String> reactivo con el nombre.
     */
    override fun getLocalUsername(): Flow<String> = prefs.usernameLocal

    /**
     * Devuelve el email del usuario almacenado en la sesión local.
     *
     * @return Flow<String> con el email.
     */
    override fun getLocalEmail(): Flow<String> = prefs.emailLocal

    /**
     * Devuelve el avatar del usuario almacenado en la sesión local.
     *
     * @return Flow<String> con la URL del avatar.
     */
    override fun getLocalAvatar(): Flow<String> = prefs.avatarLocal

    /**
     * Devuelve el ID Token almacenado localmente.
     *
     * @return Flow<String?> con el token o null si no existe.
     */
    override fun getLocalIdToken(): Flow<String?> = prefs.idTokenLocal

    /**
     * Devuelve el Auth Code almacenado localmente.
     *
     * @return Flow<String?> con el código o null si no existe.
     */
    override fun getLocalAuthCode(): Flow<String?> = prefs.authCodeLocal

    override fun getAesToken(): Flow<String> = flowOf("")

    override suspend fun saveAesToken(token: String) = Unit

    // ---------------------------------------------------------
    // DRIVE SESSION
    // ---------------------------------------------------------

    /**
     * Devuelve el email asociado a la sesión de Google Drive.
     *
     * @return Flow<String> con el email.
     */
    override fun getDriveEmail(): Flow<String> = prefs.emailDrive

    /**
     * Devuelve el ID Token de Google Drive.
     *
     * @return Flow<String?> con el token o null si no existe.
     */
    override fun getDriveIdToken(): Flow<String?> = prefs.idTokenDrive

    /**
     * Devuelve el Auth Code de Google Drive.
     *
     * @return Flow<String?> con el código o null si no existe.
     */
    override fun getDriveAuthCode(): Flow<String?> = prefs.authCodeDrive

    // ---------------------------------------------------------
    // SAVE SESSION
    // ---------------------------------------------------------

    /**
     * Guarda la sesión local del usuario.
     *
     * @param username Nombre del usuario.
     * @param email Email del usuario.
     * @param avatarUrl URL del avatar.
     * @param token ID Token de autenticación.
     */
    override suspend fun saveLocalSession(userData: UserAuthData) =
        prefs.saveLocalSession(userData.username, userData.email, userData.avatarUrl, userData.token)

    /**
     * Guarda la sesión de Google Drive.
     *
     * @param email Email asociado a Drive.
     * @param idToken Token de autenticación.
     * @param authCode Código de autorización.
     */
    override suspend fun saveDriveSession(
        email: String,
        idToken: String?,
        authCode: String?
    ) = prefs.saveDriveSession(email, idToken, authCode)

    // ---------------------------------------------------------
    // CLEAR SESSION
    // ---------------------------------------------------------

    /**
     * Limpia únicamente la sesión local.
     */
    override suspend fun clearLocalSession() = prefs.clearLocalSession()

    /**
     * Limpia únicamente la sesión de Google Drive.
     */
    override suspend fun clearDriveSession() = prefs.clearDriveSession()

    /**
     * Limpia absolutamente todas las preferencias de sesión.
     *
     * ## Advertencia
     * Esto borra:
     * - Sesión local
     * - Sesión de Drive
     * - Tokens
     * - Emails
     * - Avatares
     */
    override suspend fun clearAll() = prefs.clearAll()
}