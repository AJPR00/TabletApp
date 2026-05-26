package com.ajpr00.tablet.data.datasource.local.db.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.ajpr00.tablet.data.datasource.local.db.entity.MediaContentEntity
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistEntity
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistMediaCrossRef

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
