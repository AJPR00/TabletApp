package com.ajpr00.visumloop.tablet.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.visumloop.tablet.domain.model.MediaContent
import com.ajpr00.visumloop.tablet.data.repository.MediaRepository
import com.ajpr00.visumloop.tablet.domain.model.MediaResult
import com.ajpr00.visumloop.tablet.util.detectFormatType
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MediaItemsViewModel @Inject constructor(
    private val repositoryMedia: MediaRepository,
) : ViewModel() {

    private var driveToken: String? = null

    fun setDriveToken(token: String) {
        driveToken = token
        Log.d("MediaItemsVM", "Drive token guardado")
    }

    fun loadFromDrive(token: String? = driveToken) {
        if (token == null) {
            Log.e("MediaItemsVM", "Drive token no definido")
            return
        }
        viewModelScope.launch {
            when (val result = repositoryMedia.listMediaFilesDrive(token)) {
                is MediaResult.Success -> _mediaItems.value = result.files
                is MediaResult.Error -> _errors.value = _errors.value + result.message
            }
        }
    }


    // Estado observable con la lista de MediaContent
    private val _mediaItems = MutableStateFlow<List<MediaContent>>(emptyList())
    val mediaItems: StateFlow<List<MediaContent>> = _mediaItems

    private val _errors = MutableStateFlow<List<String>>(emptyList())
    val errors: StateFlow<List<String>> = _errors


   // Cargar desde FTP
    fun loadFromFtp() {
        Log.d("MediaItemsVM", "Cargando archivos desde FTP...")
        viewModelScope.launch {
            try {
                val files = repositoryMedia.listMediaFilesFTP()
                _mediaItems.value = files
                Log.d("MediaItemsVM", "Archivos FTP cargados: ${files.size}")
            } catch (e: Exception) {
                Log.e("MediaItemsVM", "Error al cargar desde FTP", e)
            }
        }
    }


    /** Procesa la selección de medias desde el selector de archivos */
    fun loadMediaLocal(context: Context, uris: List<Uri>) {
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
                repositoryMedia.insertMedia(it)
            }
        }
    }

    // Marcar/desmarcar favoritos
    fun toggleFavorite(media: MediaContent) {
        _mediaItems.value = _mediaItems.value.map {
            if (it.id == media.id) it.copy(isFavorite = !it.isFavorite) else it
        }
        Log.d("MediaItemsVM", "Favorito cambiado para: ${media.name}")
    }

    // Seleccionar un media para reproducir
    fun selectMedia(media: MediaContent) {
        Log.d("MediaItemsVM", "Seleccionado para reproducción: ${media.name}")
        // Aquí podrías emitir un evento o actualizar otro StateFlow con el media seleccionado
    }
}
