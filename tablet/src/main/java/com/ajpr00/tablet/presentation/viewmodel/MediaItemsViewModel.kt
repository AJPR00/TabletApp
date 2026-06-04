package com.ajpr00.tablet.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.core.domain.model.MediaResult
import com.ajpr00.core.domain.usecase.media.SaveMediaUseCase
import com.ajpr00.core.domain.usecase.media.ListMediaDriveUseCase
import com.ajpr00.core.domain.usecase.media.ListMediaFTPUseCase
import com.ajpr00.core.domain.usecase.ToggleFavoriteUseCase
import com.ajpr00.data.mapper.toMediaContentList
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.ajpr00.data.useCase.ImportMediaListUseCase
import com.ajpr00.presentation_common.state.Estado
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@HiltViewModel
class MediaItemsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val listMediaDriveUseCase: ListMediaDriveUseCase,
    private val listMediaFTPUseCase: ListMediaFTPUseCase,
    private val saveMediaUseCase: SaveMediaUseCase,
    private val importMediaListUseCase: ImportMediaListUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _EventoState = MutableStateFlow<Estado>(Estado.Inicial)
    val eventoState: StateFlow<Estado> = _EventoState

    private val _eventos = MutableSharedFlow<String>()
    val eventos = _eventos.asSharedFlow()

    // Estado observable con la lista de MediaContent
    private val _mediaItems = MutableStateFlow<List<MediaContent>>(emptyList())
    val mediaItems: StateFlow<List<MediaContent>> = _mediaItems

    /**
     * Carga archivos desde Google Drive usando el UseCase.
     * Emite eventos en caso de error.
     */
    fun loadFromDrive() {
        viewModelScope.launch {
            when (val result = listMediaDriveUseCase()) {
                is MediaResult.Success -> {
                    _mediaItems.value = result.files
                }
                is MediaResult.Error -> {
                    showError(result.message)
                }
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

    /**
     * Carga archivos desde FTP.
     * Captura excepciones y emite un evento para la UI.
     */
    fun loadFromFtp() {
        Log.d("MediaItemsVM", "Cargando archivos desde FTP...")
        viewModelScope.launch {
            try {
                val files = listMediaFTPUseCase()
                _mediaItems.value = files
                Log.d("MediaItemsVM", "Archivos FTP cargados: ${files.size}")
            } catch (e: Exception) {
                Log.e("MediaItemsVM", "Error al cargar desde FTP", e)
                showError("Error al cargar archivos desde FTP")
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

    /**
     * Importa las URIs seleccionadas y las transforma a MediaContent.
     * Emite eventos en caso de fallo o éxito.
     */
    fun importSelectedMedia(uris: List<Uri>, playlistId: String) {
        viewModelScope.launch {
            try {
                val medias = uris.toMediaContentList(context)
                importMediaListUseCase(medias, playlistId)
                enviarEvento("Medias importadas correctamente")
            } catch (e: IllegalStateException) {
                Log.e("MediaItemsVM", "Error importando medias", e)
                showError(e.message ?: "Error al importar medias")
            } catch (e: Exception) {
                Log.e("MediaItemsVM", "Error inesperado importando medias", e)
                showError("Error al importar medias")
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
    /**
     * Alterna favorito mediante el UseCase y actualiza la lista local.
     */
    fun toggleAction(media: MediaContent) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(media)
            } catch (e: Exception) {
                Log.e("MediaItemsVM", "Error toggling favorite", e)
                showError("Error al cambiar favorito")
            }
        }
    }

    /**
     * Selecciona un media para reproducir.
     * Solo registra la selección; la reproducción la maneja el ReproductorViewModel.
     */
    fun selectMedia(media: MediaContent) {
        Log.d("MediaItemsVM", "Seleccionado para reproducción: ${media.name}")
    }

    /**
     * Emite un evento de un solo uso hacia la UI.
     */
    private fun enviarEvento(mensaje: String) {
        viewModelScope.launch {
            _eventos.emit(mensaje)
        }
    }

    /**
     * Fun auxiliar para mostrar errores desde otras capas.
     */
    fun showError(message: String) {
        enviarEvento("Error: $message")
    }
}
