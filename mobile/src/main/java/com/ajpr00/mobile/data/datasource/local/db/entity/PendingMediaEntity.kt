package com.ajpr00.mobile.data.datasource.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ajpr00.core.domain.model.FormatType
import com.ajpr00.core.domain.model.PendingStatus

@Entity(
    tableName = "pending_media",
    foreignKeys = [
        ForeignKey(
            entity = DispositivoEntity::class,
            parentColumns = ["id"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deviceId")]
)
data class PendingMediaEntity(
    @PrimaryKey val id: String,
    val deviceId: String,
    val filePath: String,
    val thumbnailPath: String,
    val type: FormatType,
    val status: PendingStatus,
    val createdAt: Long,
    val retries: Int
)
