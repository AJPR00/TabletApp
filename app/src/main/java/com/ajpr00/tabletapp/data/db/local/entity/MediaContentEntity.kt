package com.ajpr00.tabletapp.data.db.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ajpr00.tabletapp.domain.model.FormatType

@Entity(tableName = "media_content")
data class MediaContentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val path: String,
    val type: String,
    val isFavorite: Boolean = false
)