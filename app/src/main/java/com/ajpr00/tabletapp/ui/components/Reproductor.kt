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
import com.ajpr00.tabletapp.domain.model.TransitionOption
import com.ajpr00.tabletapp.domain.model.transitionFor

// Tu ViewModel
import com.ajpr00.tabletapp.ui.viewmodel.MediaBackgroundViewModel

// Corrutinas
import kotlinx.coroutines.delay


/**
 * Composable que reproduce un vídeo en pantalla completa como fondo.
 *
 * @param videoUri URI del vídeo a reproducir (puede ser local o remoto).
 */
@OptIn(UnstableApi::class, ExperimentalAnimationApi::class)
@Composable
fun Reproductor(tiempoImagen: Long, transitionOption: TransitionOption) {
    val TAG = "MediaBackground"
    val context = LocalContext.current

    val viewModel: MediaBackgroundViewModel = hiltViewModel()
    val media = viewModel.currentMedia.value
    Log.d("MediaBackground", "Media cargado: $media")

    // ExoPlayer se recuerda mientras el Composable viva
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            volume = if (viewModel.isMuted.value) 0f else 1f
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        Log.d("MediaBackground", "Vídeo terminado, avanzando al siguiente")
                        viewModel.nextMedia() // 👈 aquí pedimos al ViewModel que cambie al siguiente
                    }
                }
            })
        }
    }

    LaunchedEffect(media?.path) {
        if (media?.type == FormatType.VIDEO) {
            exoPlayer.setMediaItem(MediaItem.fromUri(media.path))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        } else {
            exoPlayer.stop()
            delay(tiempoImagen)
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
        transitionSpec = transitionFor(transitionOption)
    ) { current ->
        when (current?.type) {
            FormatType.IMAGE -> {
                AsyncImage(
                    model = current.path,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
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
