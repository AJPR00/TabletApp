package com.ajpr00.visumloop.tablet.data.datasource.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ajpr00.visumloop.tablet.data.datasource.local.db.dao.MediaContentDao
import com.ajpr00.visumloop.tablet.data.datasource.local.db.entity.MediaContentEntity

@Database(entities = [MediaContentEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaContentDao(): MediaContentDao
}