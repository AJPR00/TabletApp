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
import com.ajpr00.data.mapper.tablet.toMediaContentList
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
        Log.d("MediaItemsVM", "ROOM_TEST → Solicitando archivos desde Google Drive...")
        viewModelScope.launch {
            when (val result = listMediaDriveUseCase()) {
                is MediaResult.Success -> {
                    Log.d("MediaItemsVM", "ROOM_TEST → Archivos recibidos desde Drive: ${result.files.size}")
                    _mediaItems.value = result.files
                }
                is MediaResult.Error -> {
                    Log.e("MediaItemsVM", "ROOM_TEST → Error cargando desde Drive: ${result.message}")
                    showError(result.message)
                }
            }
        }
    }

    /**
     * Carga archivos desde FTP.
     * Captura excepciones y emite un evento para la UI.
     */
    fun loadFromFtp() {
        Log.d("MediaItemsVM", "ROOM_TEST → Solicitando archivos desde FTP...")
        viewModelScope.launch {
            try {
                val files = listMediaFTPUseCase()
                Log.d("MediaItemsVM", "ROOM_TEST → Archivos recibidos desde FTP: ${files.size}")
                _mediaItems.value = files
            } catch (e: Exception) {
                Log.e("MediaItemsVM", "ROOM_TEST → Error al cargar desde FTP", e)
                showError("Error al cargar archivos desde FTP")
            }
        }
    }

    /**
     * Importa las URIs seleccionadas y las transforma a MediaContent.
     * Emite eventos en caso de fallo o éxito.
     */
    fun importSelectedMedia(uris: List<Uri>, playlistId: String) {
        Log.d("MediaItemsVM", "ROOM_TEST → Importando ${uris.size} medias desde selector...")
        viewModelScope.launch {
            try {
                val medias = uris.toMediaContentList(context)
                Log.d("MediaItemsVM", "ROOM_TEST → Transformación a MediaContent completada (${medias.size})")
                importMediaListUseCase(medias, playlistId)
                Log.d("MediaItemsVM", "ROOM_TEST → Importación completada en Room")
                enviarEvento("Medias importadas correctamente")
            } catch (e: IllegalStateException) {
                Log.e("MediaItemsVM", "ROOM_TEST → Error importando medias", e)
                showError(e.message ?: "Error al importar medias")
            } catch (e: Exception) {
                Log.e("MediaItemsVM", "ROOM_TEST → Error inesperado importando medias", e)
                showError("Error al importar medias")
            }
        }
    }

    fun toggleAction(media: MediaContent) {
        Log.d("MediaItemsVM", "ROOM_TEST → Alternando favorito para: ${media.name}")
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(media)
                Log.d("MediaItemsVM", "ROOM_TEST → Favorito actualizado correctamente")
            } catch (e: Exception) {
                Log.e("MediaItemsVM", "ROOM_TEST → Error toggling favorite", e)
                showError("Error al cambiar favorito")
            }
        }
    }

    fun selectMedia(media: MediaContent) {
        Log.d("MediaItemsVM", "ROOM_TEST → Media seleccionada para reproducción: ${media.name}")
    }

    private fun enviarEvento(mensaje: String) {
        Log.d("MediaItemsVM", "ROOM_TEST → Evento emitido: $mensaje")
        viewModelScope.launch {
            _eventos.emit(mensaje)
        }
    }

    fun showError(message: String) {
        Log.e("MediaItemsVM", "ROOM_TEST → Error mostrado a UI: $message")
        enviarEvento("Error: $message")
    }
}
