package com.ajpr00.mobile.data.datasource.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ajpr00.mobile.data.datasource.local.db.dao.DispositivoDao
import com.ajpr00.mobile.data.datasource.local.db.dao.PendingMediaDao
import com.ajpr00.mobile.data.datasource.local.db.entity.DispositivoEntity
import com.ajpr00.mobile.data.datasource.local.db.entity.PendingMediaEntity


@Database(
    entities = [DispositivoEntity::class, PendingMediaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dispositivoDao(): DispositivoDao
    abstract fun pendingMediaDao(): PendingMediaDao
}