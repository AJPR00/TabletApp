package com.ajpr00.visumloop.tablet.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.twotone.MenuBook
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.twotone.FastRewind
import androidx.compose.material.icons.twotone.MenuBook
import androidx.compose.material.icons.twotone.SkipNext
import androidx.compose.material.icons.twotone.SkipPrevious
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ajpr00.visumloop.tablet.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun PanelMenuOpciones(
    shape: Shape,
    configuracion: () -> Unit,
    info: () -> Unit,
    login: () -> Unit,
    ftp: () -> Unit,
    vacio: () -> Unit,
    dropBox: (isClose: Boolean) -> Unit,
    misArchivos: () -> Unit,
    favorito: (isClose: Boolean) -> Unit,
    googleDrive: (isClose: Boolean) -> Unit,
    onLock: () -> Unit,
    onClosedMenuApp: (isClose: Boolean) -> Unit
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

                CustomButtonPanel(
                    size = size,
                    icon = ImageVector.vectorResource(R.drawable.tree_structure_fill),
                    label = "FTP",
                    shape = shape,
                    onClick = ftp
                )

                CustomButtonPanel(
                    size = size,
                    icon = ImageVector.vectorResource(R.drawable.folder_star_fill),
                    label = "Favoritos",
                    shape = shape,
                    onClick = { favorito(true) }
                )

                CustomButtonPanel(
                    size = size,
                    icon = ImageVector.vectorResource(R.drawable.config),
                    label = "Configuracion",
                    shape = shape,
                    onClick = configuracion
                )
            }

            // Columna central
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                CustomButtonPanel(
                    size = size,
                    icon = ImageVector.vectorResource(R.drawable.google_drive_logo_fill),
                    label = "Google Drive",
                    shape = shape,
                    onClick = { googleDrive(true) }
                )

                CustomButtonPanel(
                    size = size,
                    icon = ImageVector.vectorResource(R.drawable.user_circle_fill),
                    label = "login",
                    shape = shape,
                    onClick = login
                )

                CustomButtonPanel(
                    size = size,
                    icon = ImageVector.vectorResource(R.drawable.info),
                    label = "Acerca de",
                    shape = shape,
                    onClick = info
                )
            }

            // Columna derecha
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CustomButtonPanel(
                    size = size,
                    icon = ImageVector.vectorResource(R.drawable.dropbox_logo_fill),
                    label = "Dropbox",
                    shape = shape,
                    onClick = { dropBox(true) }
                )

                CustomButtonPanel(
                    size = size,
                    icon = ImageVector.vectorResource(R.drawable.folder_open_fill),
                    label = "Mis Archivos",
                    shape = shape,
                    onClick = misArchivos
                )

                CustomButtonPanel(
                    size = size,
                    icon = Icons.TwoTone.MenuBook,
                    label = "",
                    shape = shape,
                    onClick = vacio
                )
            }
        }
    }
}


@Composable
fun MenuReproductorVideo(
    modifier: Modifier = Modifier,
    tapPosition: Offset,
    color: Color,
    isPlaying: Boolean,
    volume: Float,
    onPlayPause: () -> Unit,
    onMute: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onForward: () -> Unit,
    onRewind: () -> Unit,
    onMenu: () -> Unit
) {
    var menuWidth by remember { mutableStateOf(0) }
    var menuHeight by remember { mutableStateOf(0) }

    BoxWithConstraints(
        modifier = modifier
            .onGloballyPositioned { coords ->
                menuWidth = coords.size.width
                menuHeight = coords.size.height
            }
            .offset {
                // Cálculo para **centrar** el menú en el tap
                val centeredX = tapPosition.x - menuWidth / 2f
                val centeredY = tapPosition.y - menuHeight / 2f

                IntOffset(centeredX.toInt(), centeredY.toInt())
            }) {
        val size = maxHeight * 0.1f // tamaño relativo al alto del padre

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.Transparent),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Columna izquierda: retroceder
            Column {
                CustomButtonPanel(
                    size = size,
                    icon = Icons.TwoTone.FastRewind, label = "(-15) ",
                    color = color,
                    onClick = onRewind
                )
                CustomButtonPanel(
                    size = size,
                    icon = Icons.TwoTone.SkipPrevious, label = "Prev",
                    color = color,
                    onClick = onPrev
                )
            }

            // Columna central: play/pause + mute
            Column {
                CustomButtonPanel(
                    size = size,
                    icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    label = "Play/Pause",
                    color = color,
                    onClick = onPlayPause
                )

                CustomButtonPanel(
                    size = size,
                    icon = Icons.AutoMirrored.TwoTone.MenuBook, label = "MENU",
                    color = color,
                    onClick = onMenu
                )
                CustomButtonPanel(
                    size = size,
                    icon = if (volume == 0f) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    label = "Mute",
                    color = color,
                    onClick = onMute
                )
            }

            // Columna derecha: avanzar
            Column {
                CustomButtonPanel(
                    size = size,
                    icon = Icons.Filled.FastForward,
                    label = "(+15)",
                    color = color,
                    onClick = onForward
                )
                CustomButtonPanel(
                    size = size,
                    icon = Icons.TwoTone.SkipNext,
                    label = "Next",
                    color = color,
                    onClick = onNext
                )

            }
        }
    }
}


@Composable
fun MenuReproductorImage(
    modifier: Modifier = Modifier,
    tapPosition: Offset,
    color: Color,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onMenu: () -> Unit
) {
    var menuWidth by remember { mutableStateOf(0) }
    var menuHeight by remember { mutableStateOf(0) }

    BoxWithConstraints(
        modifier = modifier
            .onGloballyPositioned { coords ->
                menuWidth = coords.size.width
                menuHeight = coords.size.height
            }
            .offset {
                // Cálculo para **centrar** el menú en el tap
                val centeredX = tapPosition.x - menuWidth / 2f
                val centeredY = tapPosition.y - menuHeight / 2f

                IntOffset(centeredX.toInt(), centeredY.toInt())
            }) {

        val size = maxHeight * 0.1f // tamaño relativo al alto del padre

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.Transparent),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column {
                CustomButtonPanel(
                    size = size,
                    icon = Icons.TwoTone.SkipPrevious, label = "Prev",
                    color = color,
                    onClick = onPrev
                )
            }
            Column {
                CustomButtonPanel(
                    size = size,
                    icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    label = "Play/Pause",
                    color = color,
                    onClick = onPlayPause
                )
                CustomButtonPanel(
                    size = size,
                    icon = Icons.AutoMirrored.TwoTone.MenuBook, label = "MENU",
                    color = color,
                    onClick = onMenu
                )
            }
            Column {
                CustomButtonPanel(
                    size = size,
                    icon = Icons.TwoTone.SkipNext,
                    label = "Next",
                    color = color,
                    onClick = onNext
                )

            }
        }
    }
}

@Composable
fun CustomButtonPanel(
    modifier: Modifier = Modifier.padding(5.dp),
    shape: Shape = RoundedCornerShape(50),
    size: Dp,
    icon: ImageVector,
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
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(iconSize) // 👈 aquí aplicas el tamaño
            )
            Text(text = label, textAlign = TextAlign.Center)
        }
    }
}