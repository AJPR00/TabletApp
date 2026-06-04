package com.ajpr00.tablet.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ajpr00.tablet.presentation.viewmodel.MediaItemsViewModel
import com.ajpr00.tablet.presentation.viewmodel.ReproducorViewModel
import com.ajpr00.components.cards.MediaCardGrid

// --------------------------------------------------------------
// PANTALLA PRINCIPAL DEL EXPLORADOR DE MEDIOS
// --------------------------------------------------------------
// Esta pantalla muestra una cuadrícula con todas las imágenes y vídeos
// que la tablet tiene disponibles. Es como una "galería".
// Además, entra con una animación lateral para que quede más elegante.
// --------------------------------------------------------------
@Composable
fun ScreenMediaExplorer(
    viewModelMediaItems: MediaItemsViewModel,
    viewModelMediaBackground: ReproducorViewModel,
    label: String = "",
) {
    // Obtenemos la lista de medios desde el ViewModel del reproductor.
    // Esto es reactivo: si la lista cambia, Compose redibuja la UI.
    val lis by viewModelMediaBackground.mediaList.collectAsState()

    // Controla si la animación de entrada debe ejecutarse.
    var animateIn by remember { mutableStateOf(false) }

    // Cuando la pantalla aparece por primera vez, activamos la animación.
    LaunchedEffect(Unit) {
        animateIn = true
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // --------------------------------------------------------------
        // ANIMACIÓN DE ENTRADA Y SALIDA
        // --------------------------------------------------------------
        // La pantalla entra desde la derecha y sale hacia la derecha.
        // Esto da sensación de "ventana deslizante".
        // --------------------------------------------------------------
        AnimatedVisibility(
            visible = animateIn,
            modifier = Modifier.align(Alignment.CenterEnd),
            enter = slideInHorizontally(
                initialOffsetX = { full -> full }, // entra desde la derecha
                animationSpec = tween(durationMillis = 1500)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { full -> full }, // sale hacia la derecha
                animationSpec = tween(durationMillis = 1500)
            )
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .border(
                        color = Color.Black,
                        width = 5.dp,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ),
            ) {
                // Título centrado arriba
                Text(label, modifier = Modifier.align(Alignment.TopCenter))

                // --------------------------------------------------------------
                // GRID DE ELEMENTOS (IMÁGENES Y VÍDEOS)
                // --------------------------------------------------------------
                // GridCells.Adaptive hace que las tarjetas se ajusten al tamaño
                // disponible, manteniendo un mínimo de 150dp.
                // --------------------------------------------------------------
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    columns = GridCells.Adaptive(minSize = 60.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(lis) { item ->
                        MediaCardGrid(
                            media = item,
                            onClick = {}, // Aquí podrías abrir un detalle
                            onToggleAccion = {
                                viewModelMediaItems.toggleAction(item)
                            }
                        )
                    }
                }
            }
        }
    }
}