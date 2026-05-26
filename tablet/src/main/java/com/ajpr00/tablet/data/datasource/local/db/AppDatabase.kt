package com.ajpr00.tablet.data.datasource.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ajpr00.tablet.data.datasource.local.db.dao.MediaContentDao
import com.ajpr00.tablet.data.datasource.local.db.dao.PlaylistDao
import com.ajpr00.tablet.data.datasource.local.db.entity.MediaContentEntity
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistEntity
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistMediaCrossRef

@Database(
    entities = [
        MediaContentEntity::class,
        PlaylistEntity::class,
        PlaylistMediaCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mediaContentDao(): MediaContentDao
    abstract fun playlistDao(): PlaylistDao
}
