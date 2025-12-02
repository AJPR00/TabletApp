package com.ajpr00.visumloop.tablet.data.db.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_content")
data class MediaContentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val path: String,
    val type: String,
    val isFavorite: Boolean = false
)