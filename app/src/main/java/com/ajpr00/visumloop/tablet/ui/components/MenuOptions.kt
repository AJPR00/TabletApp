package com.ajpr00.visumloop.tablet.ui.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ajpr00.visumloop.tablet.R
import com.ajpr00.visumloop.tablet.data.auth.GoogleAuth
import com.ajpr00.visumloop.tablet.presentation.viewmodel.AuthViewModel
import com.ajpr00.visumloop.tablet.presentation.viewmodel.GoogleDriveViewModel
import com.ajpr00.visumloop.tablet.presentation.viewmodel.MediaBackgroundViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
@Composable
fun MenuOptions(
    viewModelMediaBackground: MediaBackgroundViewModel = hiltViewModel(),
    viewModelAuth: AuthViewModel = hiltViewModel(),
    viewModelGoogleDriveViewModel: GoogleDriveViewModel = hiltViewModel(),
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val activityContex = context as Activity
    val googleAuth = GoogleAuth(context.getString(R.string.web_client_id))

    // Launcher para imágenes y vídeos con permisos persistentes
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        Log.d("MenuOptions", "URIs seleccionados: $uris")
        uris.forEach { uri ->
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
            Log.d("MenuOptions", "Permiso persistente otorgado para: $uri")
        }
        viewModelMediaBackground.onMediasSelected(context, uris)
    }

    // Launcher para login
    val launcherLogin = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("MenuOptions", "Resultado login: code=${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                viewModelAuth.onGoogleAccountReceived(account)

                Log.d("GoogleSignIn", "Login OK")
                Log.d("GoogleSignIn", "Email: ${account.email}")
                Log.d("GoogleSignIn", "Id: ${account.id}")
                Log.d("GoogleSignIn", "IdToken: ${account.idToken}")
                Log.d("GoogleSignIn", "ServerAuthCode: ${account.serverAuthCode}")
            } catch (e: com.google.android.gms.common.api.ApiException) {
                Log.e("GoogleSignIn", "Error en login: ${e.statusCode}", e)
            }
        } else {
            Log.w("GoogleSignIn", "Login cancelado por el usuario")
        }
    }

    PanelMenu(
        shape = RoundedCornerShape(10),
        oneBox = {/*TODO*/},
        twoBox = { },
        threeBox = {
            Log.d("MenuOptions", "Botón login pulsado")
            launcherLogin.launch(googleAuth.getSignInIntent(activityContex))
        },
        fourBox = { /*TODO*/ },
        fiveBox = { /*TODO*/ },
        sixBox = { },
        sevenBox = {
            Log.d("MenuOptions", "Botón selección multimedia pulsado")
            launcher.launch(arrayOf("image/*", "video/*"))
        },
        eightBox = {},
        nineBox = {
            Log.d("MenuOptions", "Botón cerrar pulsado")
            onClose()
        }
    )

    if (viewModelAuth.idToken != null) {
        Log.d("MenuOptions", "Usuario autenticado con idToken=${viewModelAuth.idToken}")
        Toast.makeText(context, "Signed in", Toast.LENGTH_SHORT).show()
    }
}

