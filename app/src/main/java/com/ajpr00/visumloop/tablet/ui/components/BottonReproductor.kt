package com.ajpr00.visumloop.tablet.ui.components

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp


@Composable
fun PanelMenu(
    shape: Shape,
    oneBox: () -> Unit,
    twoBox: () -> Unit,
    threeBox: () -> Unit,
    fourBox: () -> Unit,
    fiveBox: () -> Unit,
    sixBox: () -> Unit,
    sevenBox: () -> Unit,
    eightBox: () -> Unit,
    nineBox: () -> Unit,
) {
    BoxWithConstraints(
    ) {
        val size = maxHeight * 0.25f // tamaño relativo al alto del padre

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
                    icon = Icons.TwoTone.FastRewind,
                    label = "(-15) ",
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    shape = shape,
                    onClick = oneBox
                )
                CustomButtonPanel(
                    size = size,
                    icon = Icons.TwoTone.SkipPrevious,
                    label = "Driver Cargar",
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    shape = shape,
                    onClick = { twoBox
                    Log.d("MenuOptions", "onClick-> twoBox") }
                )
                CustomButtonPanel(
                    size = size,
                    icon = Icons.TwoTone.SkipPrevious,
                    label = "login",
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    shape = shape,
                    onClick = threeBox
                )
            }

            // Columna central: play/pause + mute
            Column {
                CustomButtonPanel(
                    size = size,
                    icon = Icons.Filled.Pause,
                    label = "Play/Pause",
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    shape = shape,
                    onClick = fourBox
                )

                CustomButtonPanel(
                    size = size,
                    icon = Icons.TwoTone.MenuBook, label = "MENU",
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    shape = shape,
                    onClick = fiveBox
                )
                CustomButtonPanel(
                    size = size,
                    icon = Icons.Filled.VolumeUp,
                    label = "Añadir nueva cuenta",
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    shape = shape,
                    onClick = sixBox
                )
            }

            // Columna derecha: avanzar
            Column {
                CustomButtonPanel(
                    size = size,
                    icon = Icons.Filled.FastForward,
                    label = "(+15)",
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    shape = shape,
                    onClick = sevenBox
                )
                CustomButtonPanel(
                    size = size,
                    icon = Icons.TwoTone.SkipNext,
                    label = "Iniciar sesion goodle",
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    shape = shape,
                    onClick = { eightBox()
                    Log.d("MenuOptions", "onClick-> eightBox") }

                )
                CustomButtonPanel(
                    size = size,
                    icon = Icons.TwoTone.SkipNext,
                    label = "Next",
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    shape = shape,
                    onClick = nineBox

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
    iconSize: Dp = 15.dp,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.size(size),
        shape = shape,
        colors = ButtonDefaults.textButtonColors(
            containerColor = color,
            contentColor = Color.Black
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.Black,
                modifier = Modifier.size(iconSize) // 👈 aquí aplicas el tamaño
            )
            Text(text = label, textAlign = TextAlign.Center)
        }
    }
}