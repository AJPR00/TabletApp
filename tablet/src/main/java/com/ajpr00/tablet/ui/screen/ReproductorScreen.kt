package com.ajpr00.tablet.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ajpr00.components.components.showToast
import com.ajpr00.presentation_common.viewmodel.AuthViewModel
import com.ajpr00.tablet.presentation.viewmodel.MediaItemsViewModel
import com.ajpr00.tablet.presentation.viewmodel.ReproducorViewModel
import com.ajpr00.tablet.ui.components.MenuApp
import com.ajpr00.tablet.ui.components.MenuGestoReproductor
import com.ajpr00.tablet.ui.components.Reproductor
import com.ajpr00.tablet.ui.components.ScreenMediaExplorer

@SuppressLint("MultipleAwaitPointerEventScopes")
@Composable
fun ReproductorScreen(
    viewModelMediaBackground: ReproducorViewModel,
    viewModelMediaItems: MediaItemsViewModel,
    viewModelAuth: AuthViewModel,
    goToLogin: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModelMediaBackground.eventos.collect { mensaje ->
            showToast(context, mensaje)
        }
    }

    LaunchedEffect(Unit) {
        viewModelMediaItems.eventos.collect { mensaje ->
            showToast(context, mensaje)
        }
    }

    LaunchedEffect(Unit) {
        viewModelAuth.eventos.collect { mensaje ->
            showToast(context, mensaje)
        }
    }


    Box(Modifier.fillMaxSize()) {

        // El reproductor
        Reproductor(viewModelMediaBackground)

        // El overlay encima
        MenuOverlayScreen(
            viewModelMediaBackground = viewModelMediaBackground,
            menuGestoReproducion = {
                MenuGestoReproductor(
                    isVideo = viewModelMediaBackground.isVideo.collectAsState().value,
                    onPaused = { viewModelMediaBackground.togglePlayPause() },
                    onPrevMedia = { viewModelMediaBackground.prevMedia() },
                    onNextMedia = { viewModelMediaBackground.nextMedia() },
                    onRewind = { viewModelMediaBackground.rewind() },
                    onForward = { viewModelMediaBackground.forward() },
                    onSeekForward = { viewModelMediaBackground.forward(it) },
                    onSeekBackward = { viewModelMediaBackground.rewind(it) },
                    onVolumeChange = { delta ->
                        viewModelMediaBackground.setVolume(
                            viewModelMediaBackground.option.value.volume + delta
                        )
                    },
                    onLockScreen = { viewModelMediaBackground.togglesLockScreen() },
                    onShowMenu = { viewModelMediaBackground.isShowMenuApp() },
                    resetTimer = { viewModelMediaBackground.updateLastInteraction() }
                )
            },
            menuApp = {
                MenuApp(
                    viewModelMediaBackground = viewModelMediaBackground,
                    viewModelMediaItme = viewModelMediaItems,
                    goToLogin = goToLogin,
                    viewModelAuth = viewModelAuth,
                )
            },
            panelSelectorMedia = {
                ScreenMediaExplorer(
                    viewModelMediaItems = viewModelMediaItems,
                    viewModelMediaBackground = viewModelMediaBackground,
                )
            }
        )
    }
}
