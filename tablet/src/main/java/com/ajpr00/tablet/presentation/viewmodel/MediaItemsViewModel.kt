package com.ajpr00.tablet.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.core.domain.model.MediaResult
import com.ajpr00.core.domain.usecase.SaveMediaUseCase
import com.ajpr00.core.domain.usecase.ListMediaDriveUseCase
import com.ajpr00.core.domain.usecase.ListMediaFTPUseCase
import com.ajpr00.core.domain.usecase.ToggleFavoriteUseCase
import com.ajpr00.data.mapper.toMediaContentList
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.ajpr00.data.useCase.ImportMediaListUseCase
import com.ajpr00.presentation_common.state.EstadoEvento
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class MediaItemsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val listMediaDriveUseCase: ListMediaDriveUseCase,
    private val listMediaFTPUseCase: ListMediaFTPUseCase,
    private val saveMediaUseCase: SaveMediaUseCase,
    private val importMediaListUseCase: ImportMediaListUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _EventoState = MutableStateFlow<EstadoEvento>(EstadoEvento.Inicial)
    val eventoState: StateFlow<EstadoEvento> = _EventoState

    // Estado observable con la lista de MediaContent
    private val _mediaItems = MutableStateFlow<List<MediaContent>>(emptyList())
    val mediaItems: StateFlow<List<MediaContent>> = _mediaItems

    private val _errors = MutableStateFlow<List<String>>(emptyList())
    val errors: StateFlow<List<String>> = _errors

    fun loadFromDrive() {
        viewModelScope.launch {
            when (val result = listMediaDriveUseCase()) {
                is MediaResult.Success -> _mediaItems.value = result.files
                is MediaResult.Error -> _EventoState.value =
                    EstadoEvento.Mensajes(listOf(result.message))
            }
        }
    }

    /*  fun loadFromDrive() {
          viewModelScope.launch {
              val tokenValue = loginPreferences.idTokenDrive.firstOrNull()

              if (tokenValue == null) {
                  Log.e("MediaItemsVM", "Drive token no definido")
                  return@launch
              }

              val mediaResult = repositoryMedia.listMediaFilesDrive(tokenValue)
              Log.d("MediaItemsVM", "Cargando archivos desde Google Drive...$mediaResult")
              when (mediaResult) {
                  is MediaResult.Success -> _mediaItems.update { mediaResult.files.toList() }
                  is MediaResult.Error -> _errors.value = _errors.value + mediaResult.message
              }
          }
      }*/

    // Cargar desde FTP
    fun loadFromFtp() {
        Log.d("MediaItemsVM", "Cargando archivos desde FTP...")
        viewModelScope.launch {
            try {
                val files = listMediaFTPUseCase()
                _mediaItems.value = files
                Log.d("MediaItemsVM", "Archivos FTP cargados: ${files.size}")
            } catch (e: Exception) {
                Log.e("MediaItemsVM", "Error al cargar desde FTP", e)
            }
        }
    }


    /** Procesa la selección de medias desde el selector de archivos */
    /* fun loadMediaLocal(context: Context, uris: List<Uri>) {
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
                 repositoryMedia.addBd(it)
             }
         }
     }*/

    fun importSelectedMedia(uris: List<Uri>, playlistId: String) {
        viewModelScope.launch {
            try {
                val medias = uris.toMediaContentList(context)
                importMediaListUseCase(medias, playlistId)
            } catch (e: IllegalStateException) {
                _EventoState.value = EstadoEvento.Mensajes(listOf(e.message ?: "Error desconocido"))
            }
        }
    }

    // Marcar/desmarcar favoritos
    /* fun toggleFavorite(media: MediaContent) {
         viewModelScope.launch {
             if (media.isFavorite) repositoryMedia.deleteMedia(media)
             else {
                 media.isFavorite = true
                 repositoryMedia.addBd(media)
             }

             Log.d("MediaItemsVM", "Favorito cambiado para: ${media.name}")

             // Actualizar la lista en memoria
             _mediaItems.update { list ->
                 list.map {
                     if (it.path == media.path) it.copy(isFavorite = !media.isFavorite)
                     else it
                 }
             }
         }
     }*/
    fun toggleFavorite(media: MediaContent) {
        viewModelScope.launch {
            toggleFavoriteUseCase(media)
        }
    }

    // Seleccionar un media para reproducir
    fun selectMedia(media: MediaContent) {
        Log.d("MediaItemsVM", "Seleccionado para reproducción: ${media.name}")
        // Aquí podrías emitir un evento o actualizar otro StateFlow con el media seleccionado
    }

    fun clearErrors() {
        _EventoState.value = EstadoEvento.Mensajes(emptyList())
        _EventoState.value = EstadoEvento.Inicial
    }
}
