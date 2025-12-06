package com.ajpr00.visumloop.tablet.ui.screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.ajpr00.visumloop.tablet.presentation.viewmodel.MediaBackgroundViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun MenuOverlayScreen(
    viewModelMediaBackground: MediaBackgroundViewModel,
    menuGestoReproducion: @Composable () -> Unit,
    menuApp: @Composable () -> Unit,
    panelSelectorMedia: @Composable () -> Unit
) {
    Log.d("Flow", "▶ Entrando en MenuOverlayScreen")

    val estado by viewModelMediaBackground.stadoVisualMenu.collectAsState()

    var tapPosition by remember { mutableStateOf(Offset.Zero) }
    var lastInteraction by remember { mutableStateOf(System.currentTimeMillis()) }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val panelWidth = screenWidth / 3

    LaunchedEffect(lastInteraction, estado.showSidePanel) {
        if (estado.showSidePanel) {
            Log.d("Flow", "⏱ Panel lateral activo → iniciando temporizador de 20s")
            delay(20000)
            val now = System.currentTimeMillis()
            if (now - lastInteraction >= 20000) {
                viewModelMediaBackground.toggleCloseSidePanel()
                Log.d("Flow", "⏱ Panel lateral cerrado por inactividad")
            }
        }
    }
    LaunchedEffect(lastInteraction, estado.showMenuApp) {
        if (estado.showMenuApp) {
            Log.d("Flow", "⏱ Menú de app activo → iniciando temporizador de 20s")
            delay(20000)
            val now = System.currentTimeMillis()
            if (now - lastInteraction >= 20000) {
                viewModelMediaBackground.toggleShowMenuApp()
                Log.d("Flow", "⏱ Menú de app cerrado por inactividad")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(estado.showSidePanel) {
                detectTapGestures { offset ->
                    Log.d("Gestos", "👆 Tap detectado en offset=$offset")
                    if (estado.showSidePanel) {
                        val touchX = offset.x.dp
                        val sideStart = screenWidth - panelWidth
                        if (touchX < sideStart) {
                            viewModelMediaBackground.toggleCloseSidePanel()
                            Log.d("Gestos", "👆 Tap fuera del panel → cerramos panel lateral")
                            return@detectTapGestures
                        }
                    }
                    tapPosition = offset
                    viewModelMediaBackground.toggleShowMenuReproductor()
                    Log.d(
                        "Gestos",
                        "👆 Tap normal → showMenuReproductor=${estado.showMenuReproductor}"
                    )
                    viewModelMediaBackground.resetTimer()
                }
            }
            // Long press y doble tap
            .pointerInput(Unit) {
                coroutineScope {
                    detectTapGestures(
                        onPress = {
                            Log.d("Gestos", "✋ onPress detectado → esperando 3.5s para lock/unlock")
                            val job = launch {
                                delay(3500)
                                viewModelMediaBackground.toggleLockScreen()
                                Log.d(
                                    "Gestos",
                                    "✋ Long press ejecutado → onLockScreen=${estado.onLockScreen}"
                                )
                            }
                            tryAwaitRelease()
                            job.cancel()
                            Log.d("Gestos", "✋ onPress liberado antes de los 3.5s → cancelado")
                        },
                        onDoubleTap = {
                            viewModelMediaBackground.toggleShowMenuApp()
                            Log.d("Gestos", "👆👆 Doble tap → showMenu=${estado.showMenuApp}")
                            viewModelMediaBackground.resetTimer()
                        }
                    )
                }
            }
    ) {
        if (estado.showMenuReproductor) {
            Log.d("Flow", "🎵 Mostrando menú de reproducción")
            menuGestoReproducion()
        }

        AnimatedVisibility(
            visible = estado.showMenuApp,
            modifier = Modifier.align(Alignment.CenterEnd),
            enter = slideInHorizontally { full -> full },
            exit = slideOutHorizontally { full -> full }
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // Columna izquierda → menuApp
                Box(
                    modifier = Modifier
                        .weight(if (estado.showSidePanel) 2f else 1f) // mitad izquierda
                        .fillMaxHeight()
                ) {
                    menuApp()
                }

                if (estado.showSidePanel) {
                    Box(
                        modifier = Modifier
                            .weight(1f) // mitad derecha
                            .fillMaxHeight()
                    ) {
                        panelSelectorMedia()
                    }
                }
            }
        }

    }
}
