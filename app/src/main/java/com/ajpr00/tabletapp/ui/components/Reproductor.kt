package com.ajpr00.tabletapp.ui.components

// Android / sistema
import android.util.Log
import androidx.annotation.OptIn

// Compose básicos
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

// Compose layouts y animaciones
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.layout.ContentScale

// Material3

// Material Icons

// Hilt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

// ExoPlayer (Media3)
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

// Coil para imágenes
import coil.compose.AsyncImage

// Tu dominio / modelos
import com.ajpr00.tabletapp.domain.model.FormatType
import com.ajpr00.tabletapp.domain.model.transitionFor

// Tu ViewModel
import com.ajpr00.tabletapp.ui.viewmodel.MediaBackgroundViewModel

// Corrutinas
import kotlinx.coroutines.delay
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ajpr00.tabletapp.domain.model.ReproductorConfig


/**
 * Composable que reproduce un vídeo en pantalla completa como fondo.
 *
 * @param videoUri URI del vídeo a reproducir (puede ser local o remoto).
 */
@OptIn(UnstableApi::class, ExperimentalAnimationApi::class)
@Composable
fun Reproductor(viewModel: MediaBackgroundViewModel) {
    val TAG = "MediaBackground"
    val context = LocalContext.current

    val media = viewModel.currentMedia.value
    val option = viewModel.option.collectAsState().value

    Log.d("MediaBackground", "Media cargado: $media")

    // ExoPlayer se recuerda mientras el Composable viva
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        viewModel.nextMedia()
                    }
                }
            })
        }
    }

    LaunchedEffect(option.isMuted) {
        exoPlayer.volume = option.volume
    }

    LaunchedEffect(option.volume) {
        exoPlayer.volume = option.volume
    }

    LaunchedEffect(option.isPlaying) {
        exoPlayer.playWhenReady = option.isPlaying
    }

    LaunchedEffect(option.rewinds) {
        if (option.rewinds != 0) {
            val newPosition = (exoPlayer.currentPosition + option.rewinds * 1000)
                .coerceIn(0, exoPlayer.duration)
            exoPlayer.seekTo(newPosition)

            viewModel.resetRewinds()
        }
    }

    LaunchedEffect(media?.path) {
        try {
            if (media?.type == FormatType.VIDEO) {
                exoPlayer.setMediaItem(MediaItem.fromUri(media.path))
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            } else {
                exoPlayer.stop()
                delay(option.tiempoImagen)
                viewModel.nextMedia()
            }
        } catch (e: Exception) {
            Log.e("Reproductor", "Error reproduciendo medio: ${e.message}")
            viewModel.nextMedia()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "Liberando ExoPlayer")
            exoPlayer.release()
        }
    }

    AnimatedContent(
        targetState = media,
        transitionSpec = transitionFor(option.transitionOption)
    ) { current ->
        when (current?.type) {
            FormatType.IMAGE -> {
                AsyncImage(
                    model = current.path,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            FormatType.VIDEO -> {
                AndroidView(
                    factory = {
                        PlayerView(context).apply {
                            player = exoPlayer
                            useController = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                // Nada que mostrar
            }
        }
    }
}