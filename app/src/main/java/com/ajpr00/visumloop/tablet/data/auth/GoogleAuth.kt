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
    fun getGoogleSignInClient(activity: Activity): GoogleSignInClient {

        // GoogleSignInOptions es como la "lista de la compra":
        // le decimos qué datos queremos del usuario y qué permisos pedimos.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .requestIdToken(webClientId)
            .build()

        return GoogleSignIn.getClient(activity, gso)
    }

    fun getSignInIntent(activity: Activity): Intent {
        Log.d("GoogleAuth", "Obteniendo signInIntent para lanzar el login de Google")
        return getGoogleSignInClient(activity).signInIntent
    }
}
