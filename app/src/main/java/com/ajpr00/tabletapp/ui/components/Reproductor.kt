package com.ajpr00.tabletapp.ui.components

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.layout.ContentScale
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.ajpr00.tabletapp.domain.model.FormatType
import com.ajpr00.tabletapp.domain.model.transitionFor
import com.ajpr00.tabletapp.ui.viewmodel.MediaBackgroundViewModel
import kotlinx.coroutines.delay
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Reproductor principal que alterna entre imágenes y videos.
 * - Usa ExoPlayer para los videos.
 * - Para imágenes, las muestra y espera un tiempo antes de avanzar.
 * - Controla opciones como volumen, mute, rewind, autoplay, etc.
 */
@OptIn(UnstableApi::class, ExperimentalAnimationApi::class)
@Composable
fun Reproductor(viewModel: MediaBackgroundViewModel) {

    val TAG = "MediaBackground"
    val context = LocalContext.current

    // Estado actual del medio a reproducir (imagen o video)
    val media = viewModel.currentMedia.value

    // Opciones de reproducción actuales del ViewModel (volumen, mute, etc)
    val option = viewModel.option.collectAsState().value

    Log.d(TAG, "🟦 Media cargado en Reproductor(): $media")

    // Se crea y recuerda una única instancia de ExoPlayer mientras el Composable esté activo
    val exoPlayer = remember {
        Log.d(TAG, "🟩 Creando ExoPlayer")
        ExoPlayer.Builder(context).build().apply {

            // Listener para detectar fin de video
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    Log.d(TAG, "➡ Estado del player cambiado: $state")

                    // Cuando el video termina, pasamos al siguiente media
                    if (state == Player.STATE_ENDED) {
                        Log.d(TAG, "⏭ Video finalizado, avanzando al siguiente media")
                        viewModel.nextMedia()
                    }
                }
            })
        }
    }

    // ------------------------------------------------------------
    // CONTROL DE OPCIONES DEL VIEWMODEL SOBRE EXOPLAYER
    // ------------------------------------------------------------

    // Cambiar mute / sonido
    LaunchedEffect(option.isMuted) {
        Log.d(TAG, "🔇 Cambiando mute: ${option.isMuted}")
        exoPlayer.volume = option.volume
    }

    // Ajustar volumen dinámicamente
    LaunchedEffect(option.volume) {
        Log.d(TAG, "🔊 Ajustando volumen: ${option.volume}")
        exoPlayer.volume = option.volume
    }

    // Reproducir / pausar según el ViewModel
    LaunchedEffect(option.isPlaying) {
        Log.d(TAG, "▶ Cambiando play/pause: ${option.isPlaying}")
        exoPlayer.playWhenReady = option.isPlaying
    }

    // Control de rebobinado o avance rápido
    LaunchedEffect(option.rewinds) {
        if (option.rewinds != 0) {
            Log.d(TAG, "⏩ Avanzando/retrocediendo ${option.rewinds}s")

            val newPosition = (exoPlayer.currentPosition + option.rewinds * 1000)
                .coerceIn(0, exoPlayer.duration)

            exoPlayer.seekTo(newPosition)

            // Reseteamos valor para no repetirlo
            viewModel.resetRewinds()
        }
    }

    // ------------------------------------------------------------
// Cargar vídeo o imagen
// ------------------------------------------------------------
    LaunchedEffect(media?.path, option.tiempoImagen) {
        try {
            Log.d(TAG, "🔄 Cambio de media detectado: ${media?.path}  tipo=${media?.type}")

            if (media?.type == FormatType.VIDEO) {
                Log.d(TAG, "🎬 Reproduciendo VIDEO: ${media.path}")
                // Preparar y reproducir video
                exoPlayer.setMediaItem(MediaItem.fromUri(media.path))
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true

            } else if (media != null) {
                // Caso: Es una imagen
                // 1) aseguramos un tiempo mínimo razonable (ej. 2000 ms) para evitar bucles rápidos
                val minTiempo = 2000L
                val tiempo = maxOf(option.tiempoImagen, minTiempo)

                Log.d(TAG, "🖼 Mostrando IMAGEN por $tiempo ms (option=${option.tiempoImagen})")

                // Paramos el player por si estaba reproduciendo algo
                exoPlayer.stop()

                // Guardamos el id/path del media actual para comprobar después del delay
                val currentPath = media.path

                // Esperamos el tiempo configurado
                delay(tiempo)

                // Comprobación de seguridad: si el media cambió durante el delay NO llamamos nextMedia()
                val stillSame = viewModel.currentMedia.value?.path == currentPath
                Log.d(TAG, "⏱ Delay acabado. ¿sigue siendo el mismo media? $stillSame")

                if (stillSame) {
                    Log.d(TAG, "⏭ Imagen mostrada suficiente tiempo, cargando siguiente media")
                    viewModel.nextMedia()
                } else {
                    Log.d(TAG, "✋ El media cambió durante el delay — no avanzamos (evita bucle)")
                }

            } else {
                Log.w(TAG, "Media nulo en LaunchedEffect")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error reproduciendo media: ${e.message}", e)
            viewModel.nextMedia()
        }
    }


    // ------------------------------------------------------------
    // Liberar ExoPlayer cuando el composable se destruye
    // ------------------------------------------------------------
    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "🟥 Liberando ExoPlayer")
            exoPlayer.release()
        }
    }

    // ------------------------------------------------------------
    // ANIMACIÓN ENTRE IMAGEN Y VIDEO
    // ------------------------------------------------------------
    AnimatedContent(
        targetState = media,
        transitionSpec = transitionFor(option.transitionOption)
    ) { current ->

        when (current?.type) {

            // ---------------------- IMAGEN ----------------------
            FormatType.IMAGE -> {
                var zoom by remember { mutableStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }
                AsyncImage(
                    model = current.path,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoomChange, _ ->
                                // Actualiza zoom con límites (ej. 0.5x a 3x)
                                zoom = (zoom * zoomChange).coerceIn(0.5f, 3f)
                                // Opcional: Actualiza offset para pan (desplazamiento)
                                offset += pan
                            }
                        }
                        .graphicsLayer {
                            // Aplica la escala y offset
                            scaleX = zoom
                            scaleY = zoom
                            translationX = offset.x
                            translationY = offset.y
                        },
                    contentScale = ContentScale.Crop  // Mantiene proporción inicial
                )
            }

            // ----------------------- VIDEO ----------------------
            FormatType.VIDEO -> {
                AndroidView(
                    factory = {
                        PlayerView(context).apply {
                            player = exoPlayer
                            useController = false // sin controles en pantalla
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                Log.w(TAG, "⚠ Tipo de media desconocido")
            }
        }
    }
}
