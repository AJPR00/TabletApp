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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged

import com.ajpr00.visumloop.tablet.presentation.viewmodel.AuthViewModel
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
    viewModelAuth: AuthViewModel,
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

    val launcherLogin = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract()
    ) { result ->

        Log.d("LoginFlow", "--------------------------------------")
        Log.d("LoginFlow", "🔵 RESULTADO DEL LOGIN")

        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("LoginFlow", "✅ Login completado correctamente")

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
                        GoogleAuthUtil.getToken(
                            context,
                            acc,
                            "oauth2:${DriveScopes.DRIVE_READONLY}"
                        )
                    }

                    Log.d("LoginFlow", "🟢 TOKEN OBTENIDO CORRECTAMENTE:")
                    Log.d("LoginFlow", "    $token")

                    // Guardamos token
                    viewModelAuth.setDriveToken(token)
                    Log.d("LoginFlow", "💾 Token guardado en AuthViewModel")

                    viewModelMediaItme.setDriveToken(token)
                    Log.d("LoginFlow", "💾 Token pasado a MediaItemsViewModel")

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

    fun startLogin() {
        Log.d("LoginFlow", "🟦 Iniciando flujo de login con FirebaseUI")

        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder()
                .setScopes(listOf(DriveScopes.DRIVE_READONLY))
                .build()
        )

        Log.d("LoginFlow", "📌 Scopes solicitados:")
        providers.forEach {
            Log.d("LoginFlow", "    – DRIVE_READONLY")
        }

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

        Log.d("LoginFlow", "🟩 Lanzando intent de FirebaseUI...")
        launcherLogin.launch(signInIntent)
    }



    PanelMenuOpciones(
        shape = RoundedCornerShape(10),
        oneBox = { /*TODO*/ },
        twoBox = { /*TODO*/ },
        threeBox = { startLogin() },
        fourBox = { /*TODO*/ },
        fiveBox = { /*TODO*/ },
        sixBox = { viewModelMediaBackground.toggleOpenSidePanel() },
        sevenBox = { launcherMedia.launch(arrayOf("image/*", "video/*")) },
        eightBox = { viewModelMediaBackground.toggleOpenSidePanel() },
        nineBox = {
            val token = viewModelAuth.driveToken
            token?.let { viewModelMediaItme.loadFromDrive(it) }
            viewModelMediaBackground.toggleOpenSidePanel()
        },
        onLock = { viewModelMediaBackground.toggleLockScreen() },
        resetTimer = { viewModelMediaBackground.resetTimer() }
    )

    if (stateAuth.idToken != null) {
        Log.d("MenuApp", "Usuario autenticado con idToken=${stateAuth.idToken}")
        Toast.makeText(context, "Signed in", Toast.LENGTH_SHORT).show()
    }
}


@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun MenuGesto(
    isVideo: Boolean,
    onPaused: () -> Unit,
    onPrevMedia: () -> Unit,
    onNextMedia: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onVolumeChange: (delta: Float) -> Unit,
    onLock: () -> Unit,
    onMenu: () -> Unit,
    resetTimer: () -> Unit
) {
    var widthPx by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { widthPx = it.width }
            .pointerInput(isVideo, widthPx) {
                coroutineScope {
                    detectTapGestures(
                        onTap = { offset ->
                            val x = offset.x
                            val third = widthPx / 3f
                            when {
                                x < third -> {
                                    onPrevMedia()
                                    Log.d("MenuGesto", "Tap izquierda → media anterior")
                                }

                                x > 2 * third -> {
                                    onNextMedia()
                                    Log.d("MenuGesto", "Tap derecha → media siguiente")
                                }

                                else -> {
                                    onPaused()
                                    Log.d("MenuGesto", "Tap centro → pausa/play")
                                }
                            }
                        },
                        onDoubleTap = { offset ->
                            val x = offset.x
                            val third = widthPx / 3f
                            when {
                                x < third -> {
                                    if (isVideo) {
                                        onRewind()
                                        Log.d("MenuGesto", "DoubleTap izquierda → rebobinar")
                                    }
                                }

                                x > 2 * third -> {
                                    if (isVideo) {
                                        onForward()
                                        Log.d("MenuGesto", "DoubleTap derecha → adelantar")
                                    }
                                }

                                else -> {
                                    onMenu()
                                    Log.d("MenuGesto", "DoubleTap centro → mostrar menú")
                                    resetTimer()
                                }
                            }
                        },
                        onPress = {
                            val job = launch {
                                delay(3500)
                                onLock()
                                Log.d("MenuGesto", "Long press → bloquear/desbloquear")
                            }
                            tryAwaitRelease()
                            job.cancel()
                        }
                    )
                }
            }
            .pointerInput(isVideo) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    if (kotlin.math.abs(dragAmount.x) > kotlin.math.abs(dragAmount.y)) {
                        if (isVideo) {
                            if (dragAmount.x > 0) {
                                onSeekForward()
                                Log.d("MenuGesto", "Drag derecha → avanzar reproducción")
                            } else {
                                onSeekBackward()
                                Log.d("MenuGesto", "Drag izquierda → retroceder reproducción")
                            }
                        }
                    } else {
                        if (isVideo) {
                            if (dragAmount.y < 0) {
                                onVolumeChange(+0.1f)
                                Log.d("MenuGesto", "Drag arriba → subir volumen")
                            } else {
                                onVolumeChange(-0.1f)
                                Log.d("MenuGesto", "Drag abajo → bajar volumen")
                            }
                        }
                    }
                }
            }
    ) {
        Log.d("Gestos", "Estoy en MenuGesto")
    }
}
