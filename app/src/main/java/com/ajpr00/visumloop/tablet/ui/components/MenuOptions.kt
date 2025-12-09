package com.ajpr00.visumloop.tablet.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.ajpr00.visumloop.tablet.R

import com.ajpr00.visumloop.tablet.presentation.viewmodel.LoginViewModel
import com.ajpr00.visumloop.tablet.presentation.viewmodel.MediaBackgroundViewModel
import com.ajpr00.visumloop.tablet.presentation.viewmodel.MediaItemsViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MenuApp(
    viewModelMediaBackground: MediaBackgroundViewModel,
    viewModelMediaItme: MediaItemsViewModel,
    viewModelAuth: LoginViewModel,
    panelContent: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()  // CoroutineScope asociado al Composable
    val stateAuth = viewModelAuth.uiState.collectAsState().value

    val launcherMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->

        uris.forEach { uri ->
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        }
        viewModelMediaItme.loadMediaLocal(context, uris)
    }

// 👉 Launcher que gestiona el resultado del login con FirebaseUI
    val launcherLogin = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract()
    ) { result ->

        Log.d("LoginFlow", "--------------------------------------")
        Log.d("LoginFlow", "🔵 RESULTADO DEL LOGIN")

        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("LoginFlow", "✅ Login completado correctamente")

            // 👉 En este punto ya ocurrió internamente:
            // 1. FirebaseUI leyó google-services.json → obtuvo el Client ID de tu app.
            // 2. Google validó que el Client ID coincide con tu proyecto en Google Cloud.
            // 3. Se lanzó la pantalla de login del usuario → eligió cuenta y aceptó scopes.
            // 4. Google devolvió un GoogleSignInAccount vinculado a esa cuenta.

            val account = GoogleSignIn.getLastSignedInAccount(context)

            if (account == null) {
                Log.e("LoginFlow", "❌ GoogleSignInAccount es NULL después del login")
                return@rememberLauncherForActivityResult
            }

            Log.d("LoginFlow", "📌 Cuenta obtenida:")
            Log.d("LoginFlow", "    • Email: ${account.email}")
            Log.d("LoginFlow", "    • ID: ${account.id}")
            Log.d("LoginFlow", "    • Account (Android): ${account.account}")

            val acc = account.account
            if (acc == null) {
                Log.e("LoginFlow", "❌ account.account es NULL → no puedo obtener AccessToken")
                return@rememberLauncherForActivityResult
            }

            Log.d("LoginFlow", "🔑 Obteniendo token OAuth2 para Drive...")

            scope.launch {
                try {
                    val token = withContext(Dispatchers.IO) {
                        // 👉 Aquí pedimos el Access Token real para el scope DRIVE_READONLY
                        // Este token es dinámico, caduca y se usa en llamadas a la API.
                        GoogleAuthUtil.getToken(
                            context,
                            acc,
                            "oauth2:${DriveScopes.DRIVE_READONLY}"
                        )
                    }

                    Log.d("LoginFlow", "🟢 TOKEN OBTENIDO CORRECTAMENTE:")
                    Log.d("LoginFlow", "    $token")

                    // 👉 Guardamos el token en el ViewModel de autenticación
                    viewModelAuth.setDriveToken(token)
                    Log.d("LoginFlow", "💾 Token guardado en AuthViewModel")

                    // 👉 También lo pasamos al ViewModel de media items
                    viewModelMediaItme.setDriveToken(token)
                    Log.d("LoginFlow", "💾 Token pasado a MediaItemsViewModel")

                    // 👉 Con el token ya podemos llamar a la API de Drive
                    Log.d("LoginFlow", "📡 Cargando archivos desde Google Drive...")
                    viewModelMediaItme.loadFromDrive(token)

                } catch (e: Exception) {
                    Log.e("LoginFlow", "❌ ERROR obteniendo token: ${e.localizedMessage}", e)
                }
            }

        } else {
            Log.w("LoginFlow", "⚠️ Login cancelado por el usuario o fallido")
            Toast.makeText(context, "Login cancelado", Toast.LENGTH_SHORT).show()
        }

        Log.d("LoginFlow", "--------------------------------------")
    }

    // 👉 Función que inicia el flujo de login con FirebaseUI
    fun startLogin() {
        Log.d("LoginFlow", "🟦 Iniciando flujo de login con FirebaseUI")

        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder()//Compruebame si estoy en googleCloud y adems quiero pedir permiso para drive
                // Aquí defines el proveedor Google.
                // Internamente FirebaseUI usará el Client ID de google-services.json
                // para validar que tu app está registrada en Google Cloud.
                // Si falla, es porque el Client ID no coincide o no está dado de alta.
                .setScopes(listOf(DriveScopes.DRIVE_READONLY)) // 👉 Solicitud de permisos (scopes).
                // Esto no da el token aún, solo indica que quieres acceso de lectura a Drive.
                .build(),
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        Log.d("LoginFlow", "📌 Scopes solicitados:")
        providers.forEach { _ ->
            Log.d("LoginFlow", "    – DRIVE_READONLY")
        }

        // Aquí FirebaseUI construye el intent:
        // - Consulta google-services.json → obtiene el Client ID.
        // - Prepara la petición de login con Google y los scopes.
        // - Cuando se lance, Google validará el Client ID antes de mostrar la pantalla al usuario.
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

        Log.d("LoginFlow", "🟩 Lanzando intent de FirebaseUI...")
        launcherLogin.launch(signInIntent)
    }

    //  PanelMenuOpciones(
    //  shape = RoundedCornerShape(10),
    //   oneBox = { /*TODO*/ },
    //   twoBox = { /*TODO*/ },
    //   threeBox = { },
    //    fourBox = { /*TODO*/ },
    //   fiveBox = { /*TODO*/ },
    //   sixBox = { viewModelMediaBackground.isShowSidePanel(it) },
    //    sevenBox = { launcherMedia.launch(arrayOf("image/*", "video/*")) },
    //   eightBox = { viewModelMediaBackground.isShowSidePanel(it) },
    //   nineBox = {
    //       val token = viewModelAuth.driveToken
    //       viewModelMediaItme.loadFromDrive(token)
    //       token?.let { viewModelMediaItme.loadFromDrive(it) }
    //       viewModelMediaBackground.isShowSidePanel(it)
    //   },
    //   onLock = { viewModelMediaBackground.togglesLockScreen() },
    //  onClosedMenuApp = { viewModelMediaBackground.isShowMenuApp(it) }
    //)

    panelContent()

    if (stateAuth.idToken != null) {
        Log.d("MenuApp", "Usuario autenticado con idToken=${stateAuth.idToken}")
        Toast.makeText(context, "Signed in", Toast.LENGTH_SHORT).show()
    }
}



