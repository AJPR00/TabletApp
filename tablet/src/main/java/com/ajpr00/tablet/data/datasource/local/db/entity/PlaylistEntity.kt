package com.ajpr00.tablet.data.datasource.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidad que representa una playlist creada por el usuario.
 *
 * - `id`: UUID generado automáticamente.
 * - `name`: nombre único de la playlist.
 * - `updatedAt`: marca temporal para ordenación o sincronización.
 *
 * El índice único sobre `name` evita duplicidad de playlists.
 */
@Entity(
    tableName = "playlist",
    indices = [Index(value = ["name"], unique = true)]
)
data class PlaylistEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val updatedAt: Long = System.currentTimeMillis()
)


/**
 * Tabla intermedia many-to-many entre playlists y medias.
 *
 * Cada fila representa una relación:
 *  - `playlistId`: ID de la playlist.
 *  - `mediaId`: ID del archivo multimedia.
 *  - `position`: orden dentro de la playlist.
 *
 * La clave primaria compuesta evita duplicidad de relaciones.
 */
@Entity(
    primaryKeys = ["playlistId", "mediaId"],
    tableName = "playlist_media"
)
data class PlaylistMediaCrossRef(
    val playlistId: String,
    val mediaId: String,
    val position: Int
)

