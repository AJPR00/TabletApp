package com.ajpr00.tablet.ui.screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ajpr00.components.components.showToast
import com.ajpr00.tablet.presentation.viewmodel.ReproducorViewModel
import com.ajpr00.tablet.R
import com.ajpr00.tablet.presentation.viewmodel.PairingViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun MenuOverlayScreen(
    viewModelMediaBackground: ReproducorViewModel,
    viewModelPairing: PairingViewModel,
    menuGestoReproducion: @Composable () -> Unit,
    menuApp: @Composable () -> Unit,
    panelSelectorMedia: @Composable () -> Unit
) {
    val context = LocalContext.current
    Log.d("Flow", "▶ Entrando en MenuOverlayScreen")
    val overlayState by viewModelMediaBackground.stadoVisualMenu.collectAsState()
    var showLockIcon by rememberSaveable { mutableStateOf(false) }

    val statePairing by viewModelPairing.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModelMediaBackground.eventos.collect { mensaje ->
            showToast(context, mensaje)
        }
    }

    LaunchedEffect(overlayState.lastInteraction, overlayState.showSidePanel, overlayState.showMenuApp) {
        if (overlayState.showSidePanel || overlayState.showMenuApp) {
            Log.d("FlowRelo", "⏱ Algún menú activo → iniciando temporizador de 20s")

            // Capturamos el momento de inicio
            val start = overlayState.lastInteraction
            Log.d("FlowRelo", "⏱ Comienzo del temporizador: $start")
            // Esperamos 20s
            delay(50000)

            // Comprobamos si hubo interacción nueva
            val now = System.currentTimeMillis()
            Log.d("FlowRelo", "⏱ Fin del temporizador: $now")
            val elapsed = now - start

            Log.d("FlowRelo", "⏱ Tiempo transcurrido: $elapsed")

            if (elapsed >= 50000) {
                // No hubo interacción → cerramos
                if (overlayState.showSidePanel) {
                    viewModelMediaBackground.isShowSidePanel()
                    Log.d("FlowRelo", "⏱ Panel lateral cerrado por inactividad")
                }
                if (overlayState.showMenuApp) {
                    viewModelMediaBackground.isShowMenuApp()
                    Log.d("FlowRelo", "⏱ Menú de app cerrado por inactividad")
                }
            } else {
                Log.d("FlowRelo", "⏱ Hubo interacción reciente → reinicio del temporizador")
                // El LaunchedEffect se reinicia automáticamente porque cambió lastInteraction
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Long press y doble tap
            .pointerInput(Unit) {
                if (overlayState.isLockScreen) {
                    viewModelMediaBackground.enviarEvento("Pantalla bloqueada")
                }
                coroutineScope {
                    detectTapGestures(
                        onTap = {
                            // Al hacer tap mostramos el candado
                            mostrarCandado(
                                overlayState.isLockScreen,
                                { showLockIcon = it },
                                { block -> launch { block() } }
                            )
                        },
                        onPress = {
                            mostrarCandado(
                                overlayState.isLockScreen,
                                { showLockIcon = it },
                                { block -> launch { block() } }
                            )
                            viewModelMediaBackground.updateLastInteraction()
                            Log.d("Gestos", "✋ onPress detectado → esperando 3.5s para lock/unlock")
                            val job = launch {
                                delay(3500)
                                viewModelMediaBackground.togglesLockScreen()
                                Log.d(
                                    "Gestos",
                                    "✋ Long press ejecutado → onLockScreen=${overlayState.isLockScreen}"
                                )
                            }
                            tryAwaitRelease()
                            job.cancel()
                            Log.d("Gestos", "✋ onPress liberado antes de los 3.5s → cancelado")
                        },
                        onDoubleTap = {
                            mostrarCandado(
                                overlayState.isLockScreen,
                                { showLockIcon = it },
                                { block -> launch { block() } }
                            )
                            viewModelMediaBackground.isShowMenuApp()
                            Log.d("Gestos", "👆👆 Doble tap → showMenu=${overlayState.showMenuApp}")
                        }
                    )
                }
            }

    ) {
        if (overlayState.isLockScreen) {
            if (showLockIcon) Icon(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(200.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                painter = painterResource(id = R.drawable.ic_lock),
                contentDescription = "Icono Candado"
            )
        } else {
            Log.d("Flow", "Mostrando menú de reproducción")

            menuGestoReproducion()

            AnimatedVisibility(
                visible = overlayState.showMenuApp,
                modifier = Modifier.align(Alignment.CenterEnd),
                enter = slideInHorizontally(
                    initialOffsetX = { full -> -full },
                    animationSpec = tween(durationMillis = 1500) // ← velocidad
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { full -> -full },
                    animationSpec = tween(durationMillis = 1500) // ← velocidad
                )
            ) {
                Row {
                    // Columna izquierda → menuApp
                    Box(
                        modifier = Modifier
                            .weight(if (overlayState.showSidePanel) 2f else 1f) // mitad izquierda
                    ) {
                        menuApp()
                    }

                    if (overlayState.showSidePanel) {
                        Box(
                            modifier = Modifier
                                .weight(1f) // mitad derecha
                        ) {
                            panelSelectorMedia()
                        }
                    }
                }
            }
        }

    }
    if (statePairing.showPinDialog) {
        PinDialog(
            pin = statePairing.pin,
            isPaired = statePairing.isPaired,
            onDismiss = { viewModelPairing.closePinDialog() }
        )
    }
}

private fun mostrarCandado(
    isLockScreen: Boolean,
    setShowLockIcon: (Boolean) -> Unit,
    launchDelay: (suspend () -> Unit) -> Unit
) {
    if (isLockScreen) {
        setShowLockIcon(true)
        launchDelay {
            delay(2000)
            setShowLockIcon(false)
        }
    }
}

