package com.ajpr00.tabletapp.data.db.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ajpr00.tabletapp.data.db.local.dao.MediaContentDao
import com.ajpr00.tabletapp.data.db.local.entity.MediaContentEntity

@Database(entities = [MediaContentEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaContentDao(): MediaContentDao
}
