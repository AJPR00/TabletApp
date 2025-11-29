package com.ajpr00.tabletapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ajpr00.tabletapp.ui.components.BottomReproductor
import com.ajpr00.tabletapp.ui.viewmodel.MediaBackgroundViewModel

@Composable
fun MenuOverlay(onClose: () -> Unit) {
    val viewModel: MediaBackgroundViewModel = hiltViewModel()
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000)) // negro semitransparente
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Menú de opciones", color = Color.White, fontSize = 22.sp)
            Spacer(Modifier.height(16.dp))
            BottomReproductor(
                isPlaying = viewModel.isMuted.value,
                volume = 0.5f,
                onPlayPause = { viewModel.toggleMute() },
                onMute = { viewModel.toggleMute() },
                onPrev = { viewModel.prevMedia() },
                onNext = { viewModel.nextMedia() },
                onMenosPrev = { viewModel.prevMedia() },
                onMasNext = { viewModel.nextMedia() },
                onMenu = { showMenu = true }
            )
            if (showMenu) {
                MenuOptions(onClose = { showMenu = false })
            }
        }
    }
}
