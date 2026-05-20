package com.ajpr00.mobile.data.datasource.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ajpr00.core.domain.model.FormatType
import com.ajpr00.core.domain.model.PendingStatus

@Entity(tableName = "pending_media")
data class PendingMediaEntity(
    @PrimaryKey val id: String,
    val filePath: String,
    val thumbnailPath: String,
    val type: FormatType,
    val status: PendingStatus,
    val createdAt: Long,
    val retries: Int
)
