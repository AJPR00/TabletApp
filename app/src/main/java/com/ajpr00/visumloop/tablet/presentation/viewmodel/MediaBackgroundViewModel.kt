package com.ajpr00.visumloop.tablet.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import com.ajpr00.visumloop.tablet.data.repository.MediaRepository
import com.ajpr00.visumloop.tablet.domain.model.FormatType
import com.ajpr00.visumloop.tablet.domain.model.MediaContent
import com.ajpr00.visumloop.tablet.presentation.state.EstadoMenus
import com.ajpr00.visumloop.tablet.presentation.state.ReproductorConfig
import com.ajpr00.visumloop.tablet.util.detectFormatType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 📌 MediaBackgroundViewModel
 *
 * Este ViewModel es el "cerebro" del reproductor multimedia de la app.
 * Su función principal es **centralizar y controlar** todo lo que pasa con:
 *  - La lista de archivos multimedia (imágenes y vídeos) que vienen de la BD.
 *  - El estado del reproductor (play/pause, mute, volumen, rebobinar, adelantar).
 *  - Los menús y paneles visuales (menú lateral, menú del reproductor, bloqueo de pantalla).
 *
 *    - Mantiene una sola fuente de verdad sobre qué media se está reproduciendo.
 *    - Decide si lo que se reproduce es vídeo o imagen.
 *    - Controla el índice actual y permite avanzar, retroceder o elegir aleatoriamente.
 *    - Guarda la configuración del reproductor en un StateFlow para que la UI se actualice sola.
 *
 *    Cómo funciona:
 *    1. Al iniciar, escucha la BD y carga la lista de medias.
 *    2. Si hay archivos, arranca reproduciendo el primero.
 *    3. La UI observa los StateFlow (option, mediaList, currentMedia, etc.) y se redibuja.
 *    4. Cuando el usuario interactúa (mute, play/pause, siguiente, etc.), el ViewModel actualiza el estado.
 *    5. Los Logs (`Log.d`) permiten seguir paso a paso qué acción se ejecutó y cómo cambió el estado.
 *
 */

@HiltViewModel
class MediaBackgroundViewModel @Inject constructor(
    private val repository: MediaRepository
) : ViewModel() {

    // Configuración del reproductor (volumen, mute, estado de reproducción, etc.)
    private val _option = MutableStateFlow(ReproductorConfig())
    val option: StateFlow<ReproductorConfig> = _option

    val _estadoVisualMenu = MutableStateFlow(EstadoMenus())
    val stadoVisualMenu: StateFlow<EstadoMenus> = _estadoVisualMenu

    private val _lastInteraction = MutableStateFlow(System.currentTimeMillis())

    // Indica si el media actual es un vídeo
    private val _isVideo = MutableStateFlow(false)
    val isVideo = _isVideo

    private val _mediaList = MutableStateFlow<List<MediaContent>>(emptyList())
    val mediaList: StateFlow<List<MediaContent>> = _mediaList

    // Índice del media actual
    private val _currentIndex = mutableStateOf(0)
    val currentIndex: State<Int> = _currentIndex

    // Media que se está reproduciendo actualmente
    val currentMedia: State<MediaContent?> = mutableStateOf(null)

    val lastInteraction: StateFlow<Long> = _lastInteraction

    init {
        viewModelScope.launch {
            repository.getAllMediaBd().collect { list ->
                Log.d("MediaBackgroundVM", "📂 Lista recibida del repo: tamaño=${list.size}")
                _mediaList.value = list

                if (list.isNotEmpty() && currentMedia.value == null) {
                    Log.d("MediaBackgroundVM", "▶ Inicializando reproducción en índice 0")
                    playMediaAt(0)
                }
            }
        }
    }

    fun resetTimer() {
        _lastInteraction.value = System.currentTimeMillis()
        Log.d("ViewModel", "Timer reiniciado → lastInteraction=${_lastInteraction.value}")
    }

    fun toggleOpenSidePanel() {
        _estadoVisualMenu.value =
            _estadoVisualMenu.value.copy(showSidePanel = !_estadoVisualMenu.value.showSidePanel)
    }

    fun toggleShowMenuReproductor() {
        _estadoVisualMenu.value = _estadoVisualMenu.value.copy(
            showMenuReproductor = !_estadoVisualMenu.value.showMenuReproductor
        )
    }

    fun toggleShowMenuApp() {
        _estadoVisualMenu.value =
            _estadoVisualMenu.value.copy(showMenuApp = !_estadoVisualMenu.value.showMenuApp)
    }

    fun toggleCloseSidePanel() {
        _estadoVisualMenu.value =
            _estadoVisualMenu.value.copy(showSidePanel = !_estadoVisualMenu.value.showSidePanel)
    }

    fun toggleLockScreen() {
        _estadoVisualMenu.value = _estadoVisualMenu.value.copy(
            onLockScreen = !_estadoVisualMenu.value.onLockScreen,
            showMenuReproductor = !_estadoVisualMenu.value.showMenuReproductor
        )
    }

    fun isVideo(mediaContent: MediaContent): Boolean {
        return mediaContent.type == FormatType.VIDEO
    }

    /** Actualiza el volumen del reproductor */
    fun setVolume(newVolume: Float) {
        Log.d("MediaBackgroundVM", "Cambiando volumen a $newVolume")
        _option.update { current ->
            current.copy(volume = newVolume)
        }
    }

    /** Procesa la selección de medias desde el selector de archivos */
    fun onMediasSelected(context: Context, uris: List<Uri>) {
        Log.d("MediaBackgroundVM", "Se han seleccionado ${uris.size} medias")

        val newMedia = uris.map { uri ->
            Log.d("MediaBackgroundVM", "Analizando URI: $uri")

            MediaContent(
                id = 0,
                name = uri.toString(),
                path = uri.toString(),
                type = detectFormatType(context, uri),
                isFavorite = false,
            )
        }

        // Inserta cada media en la base de datos
        viewModelScope.launch {
            newMedia.forEach {
                Log.d("MediaBackgroundVM", "Insertando media en BD: $it")
                repository.insertMedia(it)
            }
        }
    }

    /** Reproduce un MediaContent directamente */
    fun playMedia(media: MediaContent) {
        val index = mediaList.value.indexOf(media)
        Log.d("MediaBackgroundVM", "playMedia(media): índice encontrado = $index")

        _currentIndex.value = index
        (currentMedia as MutableState<MediaContent?>).value = media

        // Actualiza si es vídeo
        _isVideo.value = media.type == FormatType.VIDEO

        Log.d("MediaBackgroundVM", "Reproduciendo media: $media")
    }

    /** Reproduce un media por índice */
    fun playMediaAt(index: Int) {
        if (index in mediaList.value.indices) {
            Log.d("MediaBackgroundVM", "playMediaAt(): cambiando a índice $index")

            _currentIndex.value = index
            val media = mediaList.value[index]
            (currentMedia as MutableState<MediaContent?>).value = media

            // Marca si es vídeo
            _isVideo.value = media.type == FormatType.VIDEO
            Log.d("MediaBackgroundVM", "Reproduciendo media: $media")
        } else {
            Log.e("MediaBackgroundVM", "playMediaAt(): índice fuera de rango → $index")
        }
    }

    /** Avanza al siguiente media */
    fun nextMedia() {
        val nextIndex = _currentIndex.value + 1
        Log.d("MediaBackgroundVM", "nextMedia(): índice siguiente = $nextIndex")

        if (nextIndex < mediaList.value.size) {
            playMediaAt(nextIndex)
        } else {
            Log.d("MediaBackgroundVM", "Llegó al final → reiniciando a 0")
            playMediaAt(0)
        }
    }

    /** Retrocede al media anterior */
    fun prevMedia() {
        val prevIndex = _currentIndex.value - 1
        Log.d("MediaBackgroundVM", "prevMedia(): índice anterior = $prevIndex")

        if (prevIndex >= 0) {
            playMediaAt(prevIndex)
        } else {
            Log.d("MediaBackgroundVM", "prevMedia(): ya está en el inicio")
        }
    }

    /** Reproducción aleatoria */
    fun randomMedia() {
        if (mediaList.value.isEmpty()) {
            Log.e("MediaBackgroundVM", "randomMedia(): lista vacía")
            return
        }

        val randomIndex = (0 until mediaList.value.size).random()
        Log.d("MediaBackgroundVM", "randomMedia(): índice aleatorio = $randomIndex")

        playMediaAt(randomIndex)
    }

    /** Alterna mute ON/OFF */
    fun toggleMute() {
        _option.update { current ->
            if (current.isMuted) {
                Log.d("MediaBackgroundVM", "toggleMute(): desactivando mute")
                current.copy(isMuted = false, volume = current.lastVolume)
            } else {
                Log.d("MediaBackgroundVM", "toggleMute(): activando mute")
                current.copy(isMuted = true, lastVolume = current.volume, volume = 0f)
            }
        }
    }

    /** Alterna play/pause */
    fun togglePlayPause() {
        _option.update { current ->
            Log.d("MediaBackgroundVM", "togglePlayPause(): ahora isPlaying=${!current.isPlaying}")
            current.copy(isPlaying = !current.isPlaying)
        }
    }

    /** Rebobina X segundos */
    fun rewind(seconds: Int = 15) {
        Log.d("MediaBackgroundVM", "Rebobinando $seconds segundos")
        _option.update { current ->
            current.copy(rewinds = -seconds)
        }
    }

    /** Adelanta X segundos */
    fun forward(seconds: Int = 15) {
        Log.d("MediaBackgroundVM", "Adelantando $seconds segundos")
        _option.update { current ->
            current.copy(rewinds = seconds)
        }
    }

    /** Detiene la acción de adelantar/retroceder */
    fun resetRewinds() {
        Log.d("MediaBackgroundVM", "resetRewinds(): reiniciando rewinds")
        _option.update { current ->
            current.copy(rewinds = 0)
        }
    }

}
