package com.ajpr00.tablet.data.datasource.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "media_content",
    indices = [
        Index(value = ["path"], unique = true) // evita duplicidad de path
    ]
)data class MediaContentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val path: String,
    val type: String,
)