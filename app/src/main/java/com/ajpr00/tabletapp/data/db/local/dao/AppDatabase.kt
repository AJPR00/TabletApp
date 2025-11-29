package com.ajpr00.tabletapp.data.db.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ajpr00.tabletapp.data.mapper.Converters
import com.ajpr00.tabletapp.domain.model.MediaContent

@Database(entities = [MediaContent::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaContentDao(): MediaContentDao
}
