package com.ajpr00.visumloop.tablet.data.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

// Esta clase se encarga de montar todo el "paquete" de configuración
// para que nuestra app pueda iniciar sesión con Google.
// Piensa en ella como el "puente" entre tu app y los servicios de Google.
class GoogleAuth(
    private val webClientId: String // Este es el Client ID de tipo Web que sacaste de Google Cloud Console
) {

    // Aquí construimos el cliente de Google Sign-In con las opciones que queremos.
    fun getGoogleSignInClient(activity: Activity): GoogleSignInClient {
        // Log de control para que veas qué está pasando en tiempo real
        Log.d("GoogleAuth", "Inicializando GoogleSignInClient con webClientId=$webClientId")

        // GoogleSignInOptions es como la "lista de la compra":
        // le decimos qué datos queremos del usuario y qué permisos pedimos.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail() // Queremos el email del usuario
            .requestScopes(Scope(DriveScopes.DRIVE_FILE)) // Pedimos acceso a archivos de Drive creados por la app
            .requestIdToken(webClientId) // Pedimos un ID Token → sirve para autenticar con Firebase
            .requestServerAuthCode(webClientId, true) // Pedimos un Auth Code → sirve para pedir access_token y hablar con Drive
            .build()

        // Log para confirmar qué scopes y opciones se han configurado
        Log.d("GoogleAuth", "Opciones configuradas: scopes=${gso.scopeArray.joinToString()}")

        // Finalmente devolvemos el cliente listo para usarse
        return GoogleSignIn.getClient(activity, gso)
    }

    // Este método simplemente devuelve el Intent que abre la pantalla de login de Google.
    // Piensa en él como el "botón mágico" que lanza el diálogo de selección de cuenta.
    fun getSignInIntent(activity: Activity): Intent {
        Log.d("GoogleAuth", "Obteniendo signInIntent para lanzar el login de Google")
        return getGoogleSignInClient(activity).signInIntent
    }
}
