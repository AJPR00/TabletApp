package com.ajpr00.presentation_common.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.ajpr00.core.util.VisumException
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import javax.inject.Inject

/**
 * Gestor especializado para manejar el flujo de autenticación con Facebook.
 *
 * Esta clase pertenece a la capa **presentation_common**, ya que:
 * - Interactúa directamente con el SDK de Facebook (infraestructura).
 * - Requiere un `Activity` real para funcionar (limitación del SDK).
 * - No almacena sesión ni procesa tokens (eso lo hace la capa domain).
 *
 * ## Qué hace
 * Encapsula todo el proceso de login con Facebook:
 *
 * 1. Registrar callbacks del SDK.
 * 2. Lanzar la pantalla de login.
 * 3. Recibir el resultado en `onActivityResult`.
 * 4. Entregar el token al ViewModel mediante callbacks.
 *
 * ## Por qué existe esta clase
 * El SDK de Facebook:
 * - No soporta Activity Result API.
 * - No funciona desde ViewModel ni desde Compose.
 * - Requiere obligatoriamente un `Activity` + `onActivityResult`.
 *
 * Por eso esta clase actúa como un "puente" entre la UI y el SDK.
 *
 * ## Flujo interno resumido
 * ```
 * Activity → registerCallback()
 * Activity → startLogin()
 * Facebook UI → resultado
 * Activity → onActivityResult()
 * FacebookLoginManager → callback correspondiente
 * ```
 *
 * ## Advertencias importantes
 * - No registrar el callback más de una vez (evita duplicados).
 * - No llamar a este gestor desde ViewModel.
 * - No usarlo en pantallas Compose sin Activity.
 *
 * ## Excepciones
 * Esta clase puede lanzar:
 * - [VisumException] si el SDK devuelve un error inesperado.
 */
class FacebookLoginManager @Inject constructor(
    private val callbackManager: CallbackManager
) {

    /**
     * Registra los callbacks del SDK de Facebook.
     *
     * Este método debe llamarse **una sola vez por Activity**, normalmente en `onCreate`.
     *
     * @param onSuccess Se ejecuta cuando Facebook devuelve un token válido.
     * @param onCancel Se ejecuta cuando el usuario cancela el login.
     * @param onError Se ejecuta cuando ocurre un error en el SDK.
     *
     * @throws VisumException Si el SDK devuelve un error inesperado.
     */
    fun registerCallback(
        onSuccess: (String) -> Unit,
        onCancel: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        Log.d("FacebookLoginManager", "Registrando callback de Facebook Login")

        try {
            LoginManager.getInstance().registerCallback(
                callbackManager,
                object : FacebookCallback<LoginResult> {

                    override fun onSuccess(result: LoginResult) {
                        Log.d("FacebookLoginManager", "Login exitoso. Token recibido.")
                        onSuccess(result.accessToken.token)
                    }

                    override fun onCancel() {
                        Log.d("FacebookLoginManager", "Login cancelado por el usuario.")
                        onCancel()
                    }

                    override fun onError(error: FacebookException) {
                        Log.e("FacebookLoginManager", "Error en Facebook Login: ${error.message}")
                        onError(error)
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("FacebookLoginManager", "Error registrando callback: ${e.message}")
            throw VisumException("Error registrando callback de Facebook: ${e.message}")
        }
    }

    /**
     * Inicia el flujo de login con Facebook.
     *
     * Este método abre la UI oficial de Facebook para que el usuario seleccione
     * su cuenta y otorgue permisos.
     *
     * @param activity Activity desde el cual se lanza el login.
     *
     * @throws VisumException Si el SDK falla al iniciar el login.
     */
    fun startLogin(activity: Activity) {
        Log.d("FacebookLoginManager", "Iniciando flujo de login con Facebook")

        try {
            LoginManager.getInstance().logInWithReadPermissions(
                activity,
                listOf("email", "public_profile")
            )
        } catch (e: Exception) {
            Log.e("FacebookLoginManager", "Error iniciando login: ${e.message}")
            throw VisumException("Error iniciando login con Facebook: ${e.message}")
        }
    }

    /**
     * Procesa el resultado devuelto por la UI de Facebook.
     *
     * Este método debe llamarse desde el `onActivityResult` del Activity.
     *
     * @param requestCode Código de solicitud.
     * @param resultCode Código de resultado.
     * @param data Intent con los datos devueltos por Facebook.
     *
     * @throws VisumException Si el SDK falla al procesar el resultado.
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("FacebookLoginManager", "Procesando resultado de Facebook Login")

        try {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        } catch (e: Exception) {
            Log.e("FacebookLoginManager", "Error procesando resultado: ${e.message}")
            throw VisumException("Error procesando resultado de Facebook Login: ${e.message}")
        }
    }
}
