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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ajpr00.visumloop.tablet.R

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
        providers.forEach { _ ->
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
        sixBox = { viewModelMediaBackground.isShowSidePanel(it) },
        sevenBox = { launcherMedia.launch(arrayOf("image/*", "video/*")) },
        eightBox = { viewModelMediaBackground.isShowSidePanel(it) },
        nineBox = {
            val token = viewModelAuth.driveToken
            viewModelMediaItme.loadFromDrive(token)
            token?.let { viewModelMediaItme.loadFromDrive(it) }
            viewModelMediaBackground.isShowSidePanel(it)
        },
        onLock = { viewModelMediaBackground.togglesLockScreen() },
        onClosedMenuApp = { viewModelMediaBackground.isShowMenuApp(it) }
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
    onSeekForward: (seconds: Int) -> Unit,
    onSeekBackward: (seconds: Int) -> Unit,
    onVolumeChange: (delta: Float) -> Unit,
    onLockScreen: () -> Unit,
    onShowMenu: (isClose: Boolean) -> Unit,
    resetTimer: () -> Unit
) {
    var widthPx by remember { mutableStateOf(0) }

    // Estados de feedback temporal
    var showPrev by remember { mutableStateOf(false) }
    var showNext by remember { mutableStateOf(false) }
    var showPause by remember { mutableStateOf(false) }
    var showRewind by remember { mutableStateOf(false) }
    var showForward by remember { mutableStateOf(false) }
    var showVolume by remember { mutableStateOf(false) }

    var seekOffset by remember { mutableStateOf(0f) }
    var showSeek by remember { mutableStateOf(false) }
    var volumeKey by remember { mutableStateOf(0) }
    var seekKey by remember { mutableStateOf(0) }


    val tiemShow: Long = 4000 // duración del flash (2s)

    // Nivel de volumen actual (0.0f a 1.0f)
    var volumeLevel by remember { mutableStateOf(0.5f) }

    BoxWithConstraints(
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
                                    onPrevMedia(); showPrev = true
                                }

                                x > 2 * third -> {
                                    onNextMedia(); showNext = true
                                }

                                else -> {
                                    onPaused(); showPause = true
                                }
                            }
                        },
                        onDoubleTap = { offset ->
                            val x = offset.x
                            val third = widthPx / 3f
                            when {
                                x < third -> if (isVideo) {
                                    onRewind(); showRewind = true
                                }

                                x > 2 * third -> if (isVideo) {
                                    onForward(); showForward = true
                                }

                                else -> {
                                    onShowMenu(true); resetTimer()
                                }
                            }
                        },
                        onPress = {
                            val job = launch {
                                delay(3500)
                                onLockScreen()
                            }
                            tryAwaitRelease()
                            job.cancel()
                        }
                    )
                }
            }
            .pointerInput(isVideo) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (kotlin.math.abs(dragAmount.x) < kotlin.math.abs(dragAmount.y)) {
                            // Lógica de volumen (sin cambios)
                            if (isVideo) {
                                if (dragAmount.y < 0) {
                                    volumeLevel = (volumeLevel + 0.01f).coerceIn(0f, 1f)
                                    onVolumeChange(+0.01f)
                                } else {
                                    volumeLevel = (volumeLevel - 0.01f).coerceIn(0f, 1f)
                                    onVolumeChange(-0.01f)
                                }
                                showVolume = true
                                volumeKey++
                            }
                        } else {
                            // Lógica de seek: acumular en lugar de seek inmediato
                            if (isVideo) {
                                seekOffset += dragAmount.x / 20f  // Divisor más pequeño para mayor sensibilidad (ajusta a /10f si quieres más)
                                showSeek = true
                                seekKey++
                            }
                        }
                    },
                    onDragEnd = {
                        // Aplicar el seek total acumulado al final del drag
                        if (isVideo && kotlin.math.abs(seekOffset) >= 1f) {  // Solo si es >= 1 segundo para evitar seeks insignificantes
                            val totalSeconds = seekOffset.toInt()
                            if (totalSeconds > 0) onSeekForward(totalSeconds) else onSeekBackward(-totalSeconds)
                        }
                        // Resetear para el próximo drag
                        seekOffset = 0f
                        showSeek = false
                    }
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        val iconSize = maxWidth * 0.10f

        // --- Feedback temporal ---
        if (showPrev) {
            Icon(
                painterResource(R.drawable.skip_back), "Prev",
                Modifier
                    .align(Alignment.CenterStart)
                    .size(iconSize),
                tint = MaterialTheme.colorScheme.primary
            )
            LaunchedEffect(showPrev) { delay(tiemShow); showPrev = false }
        }

        if (showNext) {
            Icon(
                painterResource(R.drawable.skip_forward), "Next",
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(iconSize),
                tint = MaterialTheme.colorScheme.primary
            )
            LaunchedEffect(showNext) { delay(tiemShow); showNext = false }
        }

        if (showPause) {
            Icon(
                painterResource(R.drawable.play), "Pause",
                Modifier
                    .align(Alignment.Center)
                    .size(iconSize),
                tint = MaterialTheme.colorScheme.primary
            )
            LaunchedEffect(showPause) { delay(tiemShow); showPause = false }
        }

        if (showRewind) {
            Icon(
                painterResource(R.drawable.rewind), "Rewind",
                Modifier
                    .align(Alignment.CenterStart)
                    .size(iconSize),
                tint = MaterialTheme.colorScheme.secondary
            )
            LaunchedEffect(showRewind) { delay(tiemShow); showRewind = false }
        }

        if (showForward) {
            Icon(
                painterResource(R.drawable.fast_forward), "Forward",
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(iconSize),
                tint = MaterialTheme.colorScheme.secondary
            )
            LaunchedEffect(showForward) { delay(tiemShow); showForward = false }
        }

        if (showVolume) {
            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val volumeIcon = when {
                    volumeLevel == 0f -> R.drawable.speaker_simple_slash
                    volumeLevel < 0.33f -> R.drawable.speaker_simple_none
                    volumeLevel < 0.66f -> R.drawable.speaker_simple_low
                    else -> R.drawable.speaker_simple_high
                }

                Icon(
                    painter = painterResource(volumeIcon),
                    contentDescription = "Volumen",
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.tertiary
                )

                Text(
                    text = "${(volumeLevel * 100).toInt()}%",
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            LaunchedEffect(volumeKey) {
                delay(tiemShow)
                showVolume = false
            }
        }

        if (showSeek) {
            Column(
                modifier = Modifier.align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val seekIcon = if (seekOffset > 0) R.drawable.fast_forward else R.drawable.rewind

                Icon(
                    painter = painterResource(seekIcon),
                    contentDescription = "Seek",
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.secondary
                )

                Text(
                    text = "${seekOffset.toInt()}s",
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            LaunchedEffect(seekKey) {
                delay(2000)
                showSeek = false
                seekOffset = 0f
            }
        }
    }
}


