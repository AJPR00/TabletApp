package com.ajpr00.visumloop.tablet.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

import com.ajpr00.visumloop.tablet.presentation.viewmodel.MediaBackgroundViewModel

@Composable
fun MenuOverlay(viewModelMediaBackgroundViewModel: MediaBackgroundViewModel, onClose: () -> Unit) {
    var showMenuReproductor by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }

    val option = viewModelMediaBackgroundViewModel.option.collectAsState().value
    val isVideo = viewModelMediaBackgroundViewModel.isVideo.collectAsState().value
    var tapPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    tapPosition = offset
                    showMenuReproductor = true
                }
            },
    ) {
        if (showMenuReproductor) {
            if (isVideo) {
                Log.d("MenuOverlay", "showMenuReproductor: ${true}")
                MenuReproductorVideo(
                    tapPosition = tapPosition,
                    isPlaying = option.isPlaying,
                    volume = if (option.isMuted) 0f else 1f,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    onPlayPause = { viewModelMediaBackgroundViewModel.togglePlayPause() },
                    onMute = { viewModelMediaBackgroundViewModel.toggleMute() },
                    onPrev = { viewModelMediaBackgroundViewModel.prevMedia() },
                    onNext = { viewModelMediaBackgroundViewModel.nextMedia() },
                    onForward = { viewModelMediaBackgroundViewModel.forward() },
                    onRewind = { viewModelMediaBackgroundViewModel.rewind() },
                    onMenu = { showMenu = true }
                )
                VerticalSlider(
                    option.volume,
                    onValueChange = { viewModelMediaBackgroundViewModel.setVolume(it) },
                    modifier = Modifier.width(200.dp).align(alignment = Alignment.CenterEnd)
                )
            } else {
                Log.d("MenuOverlay", "showMenuReproductor: ${false}")
                MenuReproductorImage(
                    tapPosition = tapPosition,
                    isPlaying = option.isPlaying,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    onPlayPause = { viewModelMediaBackgroundViewModel.togglePlayPause() },
                    onPrev = { viewModelMediaBackgroundViewModel.prevMedia() },
                    onNext = { viewModelMediaBackgroundViewModel.nextMedia() },
                    onMenu = { showMenu = true }
                )
            }
        }
        if (showMenu) {
            showMenuReproductor = false
            MenuOptions(onClose = { showMenu = false })
        }
    }
}
