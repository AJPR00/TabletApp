package com.ajpr00.data.useCase

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.core.domain.repository.MediaRepository
import com.ajpr00.core.domain.repository.PlaylistRepository
import javax.inject.Inject

class ImportMediaListUseCase @Inject constructor(
    private val mediaRepo: MediaRepository,
    private val playlistRepo: PlaylistRepository
) {

    companion object {
        private const val TAG = "ImportMediaListUseCase"
    }

    suspend operator fun invoke(
        medias: List<MediaContent>,
        playlistId: String
    ) {

        Log.d(TAG, "Iniciando importación: ${medias.size} medias → playlist $playlistId")

        try {
            val id = playlistRepo.getOrCreatePlaylist(playlistId)
            Log.d(TAG, "Insertando medias en BD…")
            mediaRepo.insertAll(medias)
            Log.d(TAG, "Medias insertadas correctamente")

            val ids = medias.map { it.id }
            Log.d(TAG, "Insertando relaciones playlist-media: $ids")

            playlistRepo.addMediaListToPlaylistInternal(
                id,
                ids
            )

            Log.d(TAG, "Relaciones insertadas correctamente")

        } catch (e: SQLiteConstraintException) {

            Log.e(TAG, "Error de duplicado en playlist_media", e)

            throw IllegalStateException("Algunos medios ya estaban en la playlist")
        }
    }
}
