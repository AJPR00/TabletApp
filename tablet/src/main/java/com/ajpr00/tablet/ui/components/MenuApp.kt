package com.ajpr00.tablet.ui.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ajpr00.tablet.R
import com.ajpr00.tablet.presentation.viewmodel.MediaItemsViewModel
import com.ajpr00.tablet.presentation.viewmodel.ReproducorViewModel
import com.ajpr00.components.components.ButtonCustonPanel
import com.ajpr00.presentation_common.viewmodel.AuthViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MenuApp(
    viewModelMediaBackground: ReproducorViewModel,
    viewModelMediaItme: MediaItemsViewModel,
    viewModelAuth: AuthViewModel,
    goToLogin: () -> Unit,
) {
    val context = LocalContext.current
    val isLocalLoggedIn by viewModelAuth.isLoggedIn.collectAsState()
    val user by viewModelAuth.currentUser.collectAsState()

    Log.d("MenuApp", "isLocalLoggedIn: $isLocalLoggedIn")
    Log.d("MenuApp", "avatar: $user")

    val avatarPainter = if (isLocalLoggedIn && user?.avatarUrl?.isNotEmpty() == true) {
        rememberAsyncImagePainter(user?.avatarUrl)
    } else {
        painterResource(R.drawable.user_circle_fill)
    }

    val launcherMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        }
        viewModelMediaItme.importSelectedMedia(uris,"Lista Unica")
    }

    PanelMenuApp(
        configuracion = { /*TODO*/ },
        info = { /*TODO*/ },
        vacio = { /*TODO*/ },
        misArchivos = { launcherMedia.launch(arrayOf("image/*", "video/*")) },
        favorito = { viewModelMediaBackground.isShowSidePanel() },
        login = {
            if (!isLocalLoggedIn) goToLogin else viewModelAuth.logout()
        },
        ftp = {
            //goToLogin(AccesLoginType.FTP)
            viewModelMediaBackground.isShowSidePanel()
        },
        dropBox = {
            //goToLogin(AccesLoginType.DROPBOX)
            viewModelMediaBackground.isShowSidePanel()
        },
        googleDrive = {
            viewModelMediaBackground.isShowSidePanel()
            //goToLogin(AccesLoginType.DRIVE)
        },
        onClosedMenuApp = { viewModelMediaBackground.isShowMenuApp() },
        onLock = { viewModelMediaBackground.togglesLockScreen() },
        avatarIcon = avatarPainter
    )


//    PanelMenuOpciones(
//        shape = RoundedCornerShape(10),
//        oneBox = { },
//        twoBox = { },
//        threeBox = { },
//        fourBox = { },
//        fiveBox = { },
//        sixBox = { viewModelMediaBackground.isShowSidePanel(it) },
//        sevenBox = { launcherMedia.launch(arrayOf("image/*", "video/*")) },
//        eightBox = { viewModelMediaBackground.isShowSidePanel(it) },
//        nineBox = {
//            val token = viewModelAuth.driveToken
//            token?.let { viewModelMediaItme.loadFromDrive(it) }
//            viewModelMediaBackground.isShowSidePanel(it)
//        },
//        onLock = { viewModelMediaBackground.togglesLockScreen() },
//        onClosedMenuApp = { viewModelMediaBackground.isShowMenuApp(it) }
//    )

}

@Composable
private fun PanelMenuApp(
    modifier: Modifier = Modifier,
    sizeIcon: Dp = 100.dp,
    configuracion: () -> Unit,
    info: () -> Unit,
    login: () -> Unit,
    vacio: () -> Unit,
    misArchivos: () -> Unit,
    dropBox: (isClose: Boolean) -> Unit,
    ftp: (isClose: Boolean) -> Unit,
    favorito: (isClose: Boolean) -> Unit,
    googleDrive: (isClose: Boolean) -> Unit,
    onLock: () -> Unit,
    onClosedMenuApp: (isClose: Boolean) -> Unit,
    avatarIcon: Painter
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f))
            //Tap en el fondo cierra el panel
            .pointerInput(Unit) {
                coroutineScope {
                    detectTapGestures(
                        onTap = { offset ->
                            Log.d("Gestos", "Tap en fondo → cerramos menu")
                            onClosedMenuApp(false)
                        },
                        onPress = {
                            Log.d("Gestos","onPress detectado, esperando 5s para desbloquear/bloquear")
                            val job = launch {
                                delay(5000)
                                onLock()
                                Log.d("Gestos", "Long press de 5s en PanelMenu → onLock()")
                            }
                            tryAwaitRelease()
                            job.cancel()
                            Log.d("Gestos", "onPress liberado antes de los 5s → cancelado")
                        }
                    )
                }
            }
    ) {
        Log.d("Gestos", "Estoy en MenuOptions/PanelMenuOpciones")
        val size = minOf(maxWidth, maxHeight) * 0.25f
        val modifierButoon = Modifier.size(size).padding(5.dp)

        //Todo Optimizar el diseño mediante un For
        //Contenedor de botones centrado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalArrangement = Arrangement.Center,
        ) {
            // Columna izquierda
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                ButtonCustonPanel(
                    modifier = modifierButoon,
                    iconSize = sizeIcon,
                    icon = painterResource(R.drawable.tree_structure_fill),
                    label = "FTP",
                    onClick = { ftp(true) }
                )

                ButtonCustonPanel(
                    modifier = modifierButoon,
                    iconSize = sizeIcon,
                    icon = painterResource(R.drawable.folder_star_fill),
                    label = "Favoritos",
                    onClick = { favorito(true) }
                )

                ButtonCustonPanel(
                    modifier = modifierButoon,
                    iconSize = sizeIcon,
                    icon = painterResource(R.drawable.config),
                    label = "Configuracion",
                    onClick = configuracion
                )
            }

            // Columna central
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                ButtonCustonPanel(
                    modifier = modifierButoon,
                    iconSize = sizeIcon,
                    icon = painterResource(R.drawable.google_drive_logo_fill),
                    label = "Google Drive",
                    onClick = { googleDrive(true) }
                )

                ButtonCustonPanel(
                    modifier = modifierButoon,
                    iconSize = sizeIcon,
                    icon = avatarIcon,
                    label = "login",
                    onClick = login
                )

                ButtonCustonPanel(
                    modifier = modifierButoon,
                    iconSize = sizeIcon,
                    icon = painterResource(R.drawable.info),
                    label = "Acerca de",
                    onClick = info
                )
            }

            // Columna derecha
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ButtonCustonPanel(
                    modifier = modifierButoon,
                    iconSize = sizeIcon,
                    icon = painterResource(R.drawable.dropbox_logo_fill),
                    label = "Dropbox",
                    onClick = { dropBox(true) }
                )

                ButtonCustonPanel(
                    modifier = modifierButoon,
                    iconSize = sizeIcon,
                    icon = painterResource(R.drawable.folder_open_fill),
                    label = "Mis Archivos",
                    onClick = misArchivos
                )

                ButtonCustonPanel(
                    modifier = modifierButoon,
                    iconSize = sizeIcon,
                    icon = painterResource(R.drawable.folder_open_fill),
                    label = "",
                    onClick = vacio
                )
            }
        }
    }
}
