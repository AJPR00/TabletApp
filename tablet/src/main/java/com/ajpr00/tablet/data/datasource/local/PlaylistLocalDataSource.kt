package com.ajpr00.tablet.data.datasource.local

import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistEntity
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistMediaCrossRef
import com.ajpr00.tablet.data.datasource.local.db.model.PlaylistWithMediaEntityModel
import kotlinx.coroutines.flow.Flow

interface PlaylistLocalDataSource {
    fun getAll(): Flow<List<PlaylistEntity>>
    suspend fun deletePlaylist(id: String)
    suspend fun deleteCrossRef(playlistId: String, mediaId: String)
    suspend fun insertPlaylist(playlist: PlaylistEntity)
    suspend fun insertCrossRef(crossRef: PlaylistMediaCrossRef)
    suspend fun getPlaylistWithMedia(id: String): PlaylistWithMediaEntityModel

    suspend fun insertCrossRefs(crossRefs: List<PlaylistMediaCrossRef>)
    suspend fun getOrCreatePlaylist(name: String): String
}
