package com.ajpr00.tabletapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.ajpr00.tabletapp.ui.viewmodel.MediaBackgroundViewModel

@Composable
fun MenuOverlay(viewModel: MediaBackgroundViewModel, onClose: () -> Unit) {
    var showMenuReproductor by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }

    val option = viewModel.option.collectAsState().value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clickable { onClose() },
    ) {
        if (showMenuReproductor) {
            BottomMenuReproductor(
                isPlaying = option.isPlaying,
                volume = if (option.isMuted) 0f else 1f,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                onPlayPause = { viewModel.togglePlayPause() },
                onMute = { viewModel.toggleMute() },
                onPrev = { viewModel.prevMedia() },
                onNext = { viewModel.nextMedia() },
                onForward = { viewModel.forward() },
                onRewind = { viewModel.rewind() },
                onMenu = { showMenu = true }
            )
            VerticalSlider(option.volume, onValueChange = { viewModel.setVolume(it) }, modifier = Modifier.width(200.dp).align(alignment = Alignment.CenterEnd))
        }
        if (showMenu) {
            showMenuReproductor = false
            MenuOptions( viewModel = viewModel,onClose = { showMenu = false })
        }
    }
}
