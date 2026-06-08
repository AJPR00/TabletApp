package com.ajpr00.tablet.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.model.FormatType
import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.core.domain.usecase.media.GetAllMediaUseCase
import com.ajpr00.tablet.presentation.state.EstadoMenus
import com.ajpr00.tablet.presentation.state.ReproductorConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * # ReproducorViewModel
 *
 * ViewModel principal del reproductor de la tablet. Gestiona:
 *
 * ## Responsabilidades
 * - Estado del reproductor (`ReproductorConfig`): volumen, mute, play/pause, rewinds.
 * - Estado visual de los menús (`EstadoMenus`): panel lateral, menú app, bloqueo de pantalla.
 * - Lista de media obtenida desde Room mediante `GetAllMediaUseCase`.
 * - Media actual en reproducción y navegación (next/prev/random).
 * - Emisión de eventos tipo toast mediante `MutableSharedFlow`.
 *
 * ## Qué NO hace
 * - No reproduce vídeo directamente (eso lo hace el Composable del reproductor).
 * - No gestiona cifrado, pairing ni servidor.
 * - No toca Room directamente (solo usa UseCases).
 *
 * ## Flujo interno
 * - Al inicializarse, observa `mediaList`.
 * - Si hay media y no hay nada reproduciéndose → inicia en índice 0.
 * - Cada gesto del usuario actualiza `lastInteraction` para controlar el auto‑ocultado de menús.
 *
 * ## Relación con otras capas
 * - **domain**: usa `GetAllMediaUseCase`.
 * - **presentation**: `MenuOverlayScreen`, `ReproductorScreen`, paneles y menús.
 * - **ui_common**: componentes visuales que consumen su estado.
 *
 * ## Advertencias
 * - `isVideo` solo indica si el media actual es vídeo (no es un estado global).
 * - No mezclar este ViewModel con lógica de pairing o servidor.
 */
@HiltViewModel
class ReproducorViewModel @Inject constructor(
    private val getAllMediaUseCase: GetAllMediaUseCase,
) : ViewModel() {

    private val TAG = "ReproducorVM"

    private val _eventos = MutableSharedFlow<String>()
    val eventos = _eventos

    private val _option = MutableStateFlow(ReproductorConfig())
    val option: StateFlow<ReproductorConfig> = _option

    private val _estadoVisualMenu = MutableStateFlow(EstadoMenus())
    val stadoVisualMenu: StateFlow<EstadoMenus> = _estadoVisualMenu

    private val _isVideo = MutableStateFlow(false)
    val isVideo = _isVideo

    val mediaList: StateFlow<List<MediaContent>> =
        getAllMediaUseCase()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    private val _currentIndex = MutableStateFlow(0)

    val currentIndex: StateFlow<Int> = _currentIndex
    private val _currentMedia = MutableStateFlow<MediaContent?>(null)

    val currentMedia: StateFlow<MediaContent?> = _currentMedia
    init {
        Log.d(TAG, "ViewModel inicializado")

        viewModelScope.launch {
            mediaList.collect { list ->
                Log.d(TAG, "Lista de media actualizada: ${list.size} elementos")

                if (list.isNotEmpty() && currentMedia.value == null) {
                    Log.d(TAG, "No había media reproduciéndose → iniciando con índice 0")
                    playMediaAt(0)
                }
            }
        }
    }

    fun updateLastInteraction(time: Long = System.currentTimeMillis()) {
        Log.d(TAG, "⏱ updateLastInteraction(): $time")
        _estadoVisualMenu.update { it.copy(lastInteraction = time) }
    }

    fun setShowMenuReproductor(isShow: Boolean) {
        Log.d(TAG, "🎛 setShowMenuReproductor(): $isShow")
        _estadoVisualMenu.update { it.copy(showMenuReproductor = isShow) }
    }

    fun isShowMenuApp() {
        val newValue = !_estadoVisualMenu.value.showMenuApp
        Log.d(TAG, "📱 isShowMenuApp(): ${_estadoVisualMenu.value.showMenuApp}")
        if (newValue)_estadoVisualMenu.update { it.copy(showSidePanel = false) }
        _estadoVisualMenu.update { it.copy(showMenuApp = !_estadoVisualMenu.value.showMenuApp) }

        if (newValue) enviarEvento("Menú abierto")
        else enviarEvento("Menú cerrado")
    }

    fun isShowSidePanel() {
        val newValue = !_estadoVisualMenu.value.showSidePanel
        Log.d(TAG, " isShowSidePanel(): $newValue")
        _estadoVisualMenu.update { it.copy(showSidePanel = newValue) }

        if (newValue) enviarEvento("Pantalla bloqueada")
        else enviarEvento("Pantalla desbloqueada")
    }

    fun togglesLockScreen() {
        val newValue = !_estadoVisualMenu.value.isLockScreen
        Log.d(TAG, "togglesLockScreen(): $newValue")
        _estadoVisualMenu.update { it.copy(isLockScreen = newValue) }
    }

    fun isVideo(mediaContent: MediaContent): Boolean {
        val result = mediaContent.type == FormatType.VIDEO
        Log.d(TAG, "🎞 isVideo(): $result → ${mediaContent.name}")
        return result
    }

    fun setVolume(newVolume: Float) {
        Log.d(TAG, "setVolume(): $newVolume")
        _option.update { it.copy(volume = newVolume) }
    }

    fun playMedia(media: MediaContent) {
        val index = mediaList.value.indexOf(media)
        Log.d(TAG, "playMedia(): index=$index → ${media.name}")
        if (index >= 0) playMediaAt(index)
    }

    fun playMediaAt(index: Int) {
        Log.d(TAG, "🎬 playMediaAt(): index=$index")

        if (index in mediaList.value.indices) {
            val media = mediaList.value[index]
            _currentIndex.value = index
            _currentMedia.value = media
            _isVideo.value = media.type == FormatType.VIDEO

            Log.d(TAG, "Reproduciendo: ${media.name} (video=${_isVideo.value})")
        } else {
            Log.e(TAG, "playMediaAt(): índice fuera de rango → $index")
        }
    }

    fun nextMedia() {
        val nextIndex = _currentIndex.value + 1
        Log.d(TAG, "⏭ nextMedia(): $nextIndex")

        if (nextIndex < mediaList.value.size) {
            playMediaAt(nextIndex)
        } else {
            Log.d(TAG, "Fin de lista → reiniciando")
            playMediaAt(0)
        }
    }

    fun prevMedia() {
        val prevIndex = _currentIndex.value - 1
        Log.d(TAG, "⏮ prevMedia(): $prevIndex")

        if (prevIndex >= 0) {
            playMediaAt(prevIndex)
        } else {
            Log.d(TAG, "prevMedia(): ya está en el inicio")
        }
    }

    fun randomMedia() {
        if (mediaList.value.isEmpty()) {
            Log.e(TAG, "randomMedia(): lista vacía")
            return
        }

        val randomIndex = (0 until mediaList.value.size).random()
        Log.d(TAG, "randomMedia(): índice aleatorio = $randomIndex")

        playMediaAt(randomIndex)
    }

    fun toggleMute() {
        _option.update { current ->
            if (current.isMuted) {
                Log.d(TAG, "toggleMute(): desactivando mute")
                current.copy(isMuted = false, volume = current.lastVolume)
            } else {
                Log.d(TAG, "toggleMute(): activando mute")
                current.copy(isMuted = true, lastVolume = current.volume, volume = 0f)
            }
        }
    }

    fun togglePlayPause() {
        _option.update { current ->
            Log.d(TAG, "⏯ togglePlayPause(): ahora isPlaying=${!current.isPlaying}")
            current.copy(isPlaying = !current.isPlaying)
        }
    }

    fun rewind(seconds: Int = 5) {
        Log.d(TAG, "rewind(): -$seconds segundos")
        _option.update { it.copy(rewinds = -seconds) }
    }

    fun forward(seconds: Int = 5) {
        Log.d(TAG, "forward(): +$seconds segundos")
        _option.update { it.copy(rewinds = seconds) }
    }

    fun resetRewinds() {
        Log.d(TAG, "resetRewinds()")
        _option.update { it.copy(rewinds = 0) }
    }

    fun enviarEvento(mensaje: String) {
        viewModelScope.launch {
            _eventos.emit(mensaje)
        }
    }

    fun showError(message: String) {
        enviarEvento("Error: $message")
    }

}
