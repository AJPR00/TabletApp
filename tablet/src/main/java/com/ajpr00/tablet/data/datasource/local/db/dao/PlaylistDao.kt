package com.ajpr00.tablet.data.datasource.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistEntity
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistMediaCrossRef
import com.ajpr00.tablet.data.datasource.local.db.model.PlaylistWithMediaEntityModel
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlist")
    fun getAll(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefs(crossRefs: List<PlaylistMediaCrossRef>)

    @Query("DELETE FROM playlist WHERE id = :id")
    suspend fun deletePlaylist(id: String)

    //Porque una playlist puede tener muchos medias, y un media puede estar en muchas playlists. Es una relación N:M.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: PlaylistMediaCrossRef)

    //Elimina la relación entre una playlist y un media.
    @Query("DELETE FROM playlist_media WHERE playlistId = :playlistId AND mediaId = :mediaId")
    suspend fun deleteCrossRef(playlistId: String, mediaId: String)

    @Transaction
    @Query("SELECT * FROM playlist WHERE id = :playlistId")
    suspend fun getPlaylistWithMedia(playlistId: String): PlaylistWithMediaEntityModel

    @Query("SELECT * FROM playlist WHERE name = :name LIMIT 1")
    suspend fun getPlaylistByName(name: String): PlaylistEntity?


}
