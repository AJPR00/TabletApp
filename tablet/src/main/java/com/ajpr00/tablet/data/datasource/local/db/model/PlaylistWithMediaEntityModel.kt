package com.ajpr00.tablet.data.datasource.local.db.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.ajpr00.tablet.data.datasource.local.db.entity.MediaContentEntity
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistEntity
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistMediaCrossRef

/**
 * Modelo relacional que combina una playlist con todos sus medias asociados.
 *
 * Room resuelve automáticamente la relación many-to-many mediante:
 *  - `PlaylistEntity` como entidad principal.
 *  - `MediaContentEntity` como entidad relacionada.
 *  - `PlaylistMediaCrossRef` como tabla puente.
 *
 * Este modelo se usa para consultas complejas que requieren obtener
 * una playlist completa con su contenido multimedia.
 */
data class PlaylistWithMediaEntityModel(
    @Embedded val playlist: PlaylistEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistMediaCrossRef::class,
            parentColumn = "playlistId",
            entityColumn = "mediaId"
        )
    )
    val media: List<MediaContentEntity>
)
