package com.ajpr00.tabletapp.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ajpr00.tabletapp.R
import com.ajpr00.tabletapp.ui.components.MenuOverlay
import com.ajpr00.tabletapp.ui.components.Reproductor
import com.ajpr00.tabletapp.ui.viewmodel.MediaBackgroundViewModel


@SuppressLint("MultipleAwaitPointerEventScopes")
@Composable
fun ReproductorScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel: MediaBackgroundViewModel = hiltViewModel()

    var showMenu by remember { mutableStateOf(false) }
    var longPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                while (true) {
                    awaitPointerEventScope {
                        val down = awaitFirstDown()
                        showMenu = false

                        val longPress = withTimeoutOrNull(4000) {
                            // espera a que suelte el dedo
                            do {
                                val event = awaitPointerEvent()
                                if (event.changes.any { !it.pressed }) break
                            } while (true)
                        }

                        if (longPress == null) {
                            // si pasó el timeout → long press
                            longPressed = !longPressed
                        } else {
                            // si soltó antes → tap normal
                            showMenu = true
                        }
                    }

                }
            }
    ) {
        Reproductor(viewModel)

        if (longPressed) {
            AsyncImage(
                model = R.drawable.ic_ca,
                contentDescription = null,
                modifier = Modifier.size(50.dp)
                    .align(Alignment.BottomCenter),
                contentScale = ContentScale.Crop
            )
        }

        if (showMenu && !longPressed) {
            MenuOverlay(
                onClose = { showMenu = false },
                viewModel = viewModel
            )
        }
    }
}
