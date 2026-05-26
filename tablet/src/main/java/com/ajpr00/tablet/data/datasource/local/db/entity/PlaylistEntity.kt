package com.ajpr00.tablet.data.datasource.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "playlist",
    indices = [Index(value = ["name"], unique = true)])
data class PlaylistEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    primaryKeys = ["playlistId", "mediaId"],
    tableName = "playlist_media"
)
data class PlaylistMediaCrossRef(
    val playlistId: String,
    val mediaId: String,
    val position: Int
)
