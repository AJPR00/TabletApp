package com.ajpr00.presentation_common.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.ajpr00.presentation_common.viewmodel.AuthViewModel
import javax.inject.Inject

/**
 * # FacebookInitializer
 *
 * Clase auxiliar que encapsula toda la lógica necesaria para integrar
 * el **SDK oficial de Facebook Login** dentro de la capa *presentation*.
 *
 * Su objetivo principal es mantener la `Activity` limpia y sin código
 * específico del SDK, delegando toda la interacción a esta clase.
 *
 * ## ¿Por qué existe esta clase?
 * El SDK de Facebook:
 * - Requiere una `Activity` real (no funciona desde ViewModel ni Compose).
 * - Usa callbacks antiguos (`onActivityResult`).
 * - Necesita registrar listeners antes de iniciar el login.
 *
 * Para evitar ensuciar la Activity con lógica de infraestructura,
 * centralizamos todo aquí.
 *
 * ## Relación con otras capas
 * - **presentation_common** → expone eventos al `AuthViewModel`.
 * - **infraestructura** → usa `FacebookLoginManager` para hablar con el SDK.
 * - **UI (mobile)** → la Activity solo delega llamadas.
 *
 * ## Flujo interno
 * 1. `init()` registra los callbacks del SDK.
 * 2. `startLogin()` abre la UI oficial de Facebook.
 * 3. `onActivityResult()` entrega el resultado al SDK.
 *
 * ## Advertencias
 * - Debe llamarse siempre desde una Activity real.
 * - No debe contener lógica de negocio (eso es del ViewModel).
 * - No debe almacenar estado.
 */
class FacebookInitializer @Inject constructor(
    private val facebookLoginManager: FacebookLoginManager
) {

    /**
     * Registra los callbacks del SDK de Facebook y conecta los resultados
     * con el `AuthViewModel`.
     *
     * ## Parámetros
     * - `authViewModel`: ViewModel que recibirá los eventos del login.
     *
     * ## Flujo
     * - Si Facebook devuelve un token → se envía al ViewModel.
     * - Si el usuario cancela → se notifica al ViewModel.
     * - Si ocurre un error → se envía el mensaje al ViewModel.
     *
     * ## Logs
     * Se añaden logs para depurar fácilmente el flujo de autenticación.
     */
    fun init(authViewModel: AuthViewModel) {
        Log.d("FACEBOOK_INIT", "Registrando callbacks de Facebook Login")

        facebookLoginManager.registerCallback(
            onSuccess = { token ->
                Log.d("FACEBOOK_INIT", "Login exitoso. Token recibido")
                authViewModel.onFacebookAccessToken(token)
            },
            onCancel = {
                Log.d("FACEBOOK_INIT", "Login cancelado por el usuario")
                authViewModel.enviarEvento("Inicio de sesión cancelado")
            },
            onError = { error ->
                Log.e("FACEBOOK_INIT", "Error en Facebook Login: ${error.message}")
                authViewModel.enviarEvento("Error en Facebook Login: ${error.message}")
            }
        )
    }

    /**
     * Inicia el flujo de login de Facebook.
     *
     * ## Parámetros
     * - `activity`: Activity desde la cual se lanza el login.
     *
     * ## Notas
     * - Facebook obliga a usar una Activity real.
     * - No funciona desde ViewModel ni desde Compose.
     */
    fun startLogin(activity: Activity) {
        Log.d("FACEBOOK_INIT", "Iniciando flujo de login con Facebook")
        facebookLoginManager.startLogin(activity)
    }

    /**
     * Procesa el resultado devuelto por la UI de Facebook.
     *
     * ## Parámetros
     * - `requestCode`: Código de solicitud.
     * - `resultCode`: Código de resultado.
     * - `data`: Intent con los datos devueltos.
     *
     * ## Advertencias
     * - Debe llamarse desde `onActivityResult` de la Activity.
     * - No llamar desde Compose.
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("FACEBOOK_INIT", "Delegando resultado de Facebook Login al SDK")
        facebookLoginManager.onActivityResult(requestCode, resultCode, data)
    }
}
