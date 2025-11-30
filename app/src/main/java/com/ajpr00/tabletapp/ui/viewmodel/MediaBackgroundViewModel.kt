package com.ajpr00.tabletapp.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import com.ajpr00.tabletapp.data.repository.MediaRepository
import com.ajpr00.tabletapp.domain.model.MediaContent
import com.ajpr00.tabletapp.domain.model.ReproductorConfig
import com.ajpr00.tabletapp.util.detectFormatType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.map

@HiltViewModel
class MediaBackgroundViewModel @Inject constructor(
    private val repository: MediaRepository
) : ViewModel() {

    private val _option = MutableStateFlow(ReproductorConfig())
    val option: StateFlow<ReproductorConfig> = _option

    // Lista completa de media (imágenes y vídeos). StateFlow que expone la lista en tiempo real
    val mediaList: StateFlow<List<MediaContent>> = repository.getAllMedia()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Índice actual
    private val _currentIndex = mutableStateOf(0)
    val currentIndex: State<Int> = _currentIndex

    // Media actual derivado del índice
    val currentMedia: State<MediaContent?> = mutableStateOf(null)

    init {
        viewModelScope.launch {
            mediaList.collect { list ->
                if (list.isNotEmpty() && currentMedia.value == null) {
                    playMediaAt(0)
                }
            }
        }
    }

    fun setVolume(newVolume: Float) {
        _option.update { current ->
            current.copy(volume = newVolume)
        }
    }

    fun onMediasSelected(context: Context, uris: List<Uri>) {
        val newMedia = uris.map { uri ->
            Log.d("MediaBackgroundVM", "Uri seleccionada: $uri")
            MediaContent(
                id = 0,
                name = uri.toString(),
                path = uri.toString(),
                type = detectFormatType(context, uri),
                isFavorite = false,
            )
        }
        viewModelScope.launch {
            newMedia.forEach { repository.insertMedia(it) }
        }
    }

    /** Reproduce un media específico */
    fun playMedia(media: MediaContent) {
        _currentIndex.value = mediaList.value.indexOf(media)
        (currentMedia as MutableState<MediaContent?>).value = media
        Log.d("MediaBackgroundVM", "Reproduciendo media: $media")
    }

    /** Reproduce por índice */
    fun playMediaAt(index: Int) {
        if (index in mediaList.value.indices) {
            _currentIndex.value = index
            (currentMedia as MutableState<MediaContent?>).value = mediaList.value[index]
            Log.d(
                "MediaBackgroundVM",
                "Reproduciendo media en índice $index: ${mediaList.value[index]}"
            )
        }
    }

    /** Avanza al siguiente */
    fun nextMedia() {
        val nextIndex = _currentIndex.value + 1
        if (nextIndex < mediaList.value.size) {
            playMediaAt(nextIndex)
        } else {
            playMediaAt(0)
        }
    }

    /** Retrocede al anterior */
    fun prevMedia() {
        val prevIndex = _currentIndex.value - 1
        if (prevIndex >= 0) {
            playMediaAt(prevIndex)
        }
    }

    /** Reproduce aleatorio */
    fun randomMedia() {
        val randomIndex = (0 until mediaList.value.size).random()
        playMediaAt(randomIndex)
    }

    fun toggleMute() {
        _option.update { current ->
            if (current.isMuted) {
                current.copy(isMuted = false, volume = current.lastVolume)
            } else {
                current.copy(isMuted = true, lastVolume = current.volume, volume = 0f)
            }
        }
    }


    fun togglePlayPause() {
        _option.update { current ->
            current.copy(isPlaying = !current.isPlaying)
        }
    }

    fun rewind(seconds: Int = 15) {
        _option.update { current ->
            current.copy(rewinds = -seconds)
        }
    }

    fun forward(seconds: Int = 15) {
        _option.update { current ->
            current.copy(rewinds = seconds)
        }
    }

    fun resetRewinds() {
        _option.update { current ->
            current.copy(rewinds = 0)
        }
    }



}
