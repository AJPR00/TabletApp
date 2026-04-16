package com.ajpr00.visumloop.tablet.ui.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ajpr00.visumloop.tablet.R
import com.ajpr00.visumloop.tablet.domain.model.AccesLoginType
import com.ajpr00.visumloop.tablet.presentation.viewmodel.AuthViewModel
import com.ajpr00.visumloop.tablet.presentation.viewmodel.ReproducorViewModel
import com.ajpr00.visumloop.tablet.presentation.viewmodel.MediaItemsViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MenuApp(
    viewModelMediaBackground: ReproducorViewModel,
    viewModelMediaItme: MediaItemsViewModel,
    viewModelAuth: AuthViewModel,
    goToLogin: (ascessType: AccesLoginType) -> Unit,
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
        viewModelMediaItme.loadMediaLocal(context, uris)
    }

    PanelMenuApp(
        configuracion = { /*TODO*/ },
        info = { /*TODO*/ },
        vacio = { /*TODO*/ },
        misArchivos = { launcherMedia.launch(arrayOf("image/*", "video/*")) },
        favorito = { viewModelMediaBackground.isShowSidePanel(it) },
        login = {
            if (!isLocalLoggedIn) goToLogin(AccesLoginType.LOCAL) else viewModelAuth.logout()
        },
        ftp = {
            //goToLogin(AccesLoginType.FTP)
            viewModelMediaBackground.isShowSidePanel(it)
        },
        dropBox = {
            //goToLogin(AccesLoginType.DROPBOX)
            viewModelMediaBackground.isShowSidePanel(it)
        },
        googleDrive = {
            viewModelMediaBackground.isShowSidePanel(it)
            //goToLogin(AccesLoginType.DRIVE)
        },
        onClosedMenuApp = { viewModelMediaBackground.isShowMenuApp(it) },
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
    shape: Shape = RoundedCornerShape(10),
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
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f))
            // 👇 Tap en el fondo cierra el panel
            .pointerInput(Unit) {
                coroutineScope {
                    detectTapGestures(
                        onTap = { offset ->
                            Log.d("Gestos", "Tap en fondo → cerramos menu")
                            onClosedMenuApp(false)
                        },
                        onPress = {
                            Log.d(
                                "Gestos",
                                "onPress detectado, esperando 5s para desbloquear/bloquear"
                            )
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

        // 👇 Contenedor de botones centrado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalArrangement = Arrangement.Center,
        ) {
            // Columna izquierda
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                ButtonCustonPanel(
                    size = size,
                    icon = painterResource(R.drawable.tree_structure_fill),
                    label = "FTP",
                    shape = shape,
                    onClick = { ftp(true) }
                )

                ButtonCustonPanel(
                    size = size,
                    icon = painterResource(R.drawable.folder_star_fill),
                    label = "Favoritos",
                    shape = shape,
                    onClick = { favorito(true) }
                )

                ButtonCustonPanel(
                    size = size,
                    icon = painterResource(R.drawable.config),
                    label = "Configuracion",
                    shape = shape,
                    onClick = configuracion
                )
            }

            // Columna central
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                ButtonCustonPanel(
                    size = size,
                    icon = painterResource(R.drawable.google_drive_logo_fill),
                    label = "Google Drive",
                    shape = shape,
                    onClick = { googleDrive(true) }
                )

                ButtonCustonPanel(
                    size = size,
                    icon = avatarIcon,
                    label = "login",
                    shape = shape,
                    onClick = login
                )

                ButtonCustonPanel(
                    size = size,
                    icon = painterResource(R.drawable.info),
                    label = "Acerca de",
                    shape = shape,
                    onClick = info
                )
            }

            // Columna derecha
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ButtonCustonPanel(
                    size = size,
                    icon = painterResource(R.drawable.dropbox_logo_fill),
                    label = "Dropbox",
                    shape = shape,
                    onClick = { dropBox(true) }
                )

                ButtonCustonPanel(
                    size = size,
                    icon = painterResource(R.drawable.folder_open_fill),
                    label = "Mis Archivos",
                    shape = shape,
                    onClick = misArchivos
                )

                ButtonCustonPanel(
                    size = size,
                    icon = painterResource(R.drawable.folder_open_fill),
                    label = "",
                    shape = shape,
                    onClick = vacio
                )
            }
        }
    }
}

@Composable
private fun ButtonCustonPanel(
    modifier: Modifier = Modifier.padding(5.dp),
    shape: Shape = RoundedCornerShape(50),
    size: Dp,
    icon: Painter,
    iconSize: Dp = 90.dp,
    label: String,
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.size(size),
        shape = shape,
        colors = ButtonDefaults.textButtonColors(
            containerColor = color,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            /** Usamos Icon para recursos vectoriales porque permiten aplicar tint y mantienen su forma.
            Usamos Image para imágenes no vectoriales (como avatares remotos) porque respetan sus colores,
            se ajustan mejor al recorte circular y permiten ContentScale.Crop para llenar el espacio.*/

            val esVector = icon is VectorPainter

            if (esVector) {
                // 👉 Iconos vectoriales: tint + no circular
                Icon(
                    painter = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(iconSize)
                )
            } else {
                // 👉 Imágenes NO vectoriales: sin tint + circular + ajustada
                Image(
                    painter = icon,
                    contentDescription = label,
                    modifier = Modifier
                        .size(iconSize)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentScale = ContentScale.Crop   // 👈 CLAVE: ajusta la imagen al círculo
                )
            }

            Text(text = label, textAlign = TextAlign.Center)
        }
    }
}
