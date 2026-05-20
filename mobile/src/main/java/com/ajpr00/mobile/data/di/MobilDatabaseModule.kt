package com.ajpr00.mobile.data.di

import android.content.Context
import androidx.room.Room
import com.ajpr00.mobile.data.datasource.local.db.AppDatabase
import com.ajpr00.mobile.data.datasource.local.db.dao.DispositivoDao
import com.ajpr00.mobile.data.datasource.local.db.dao.PendingMediaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MobilDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mobile_database.db"
        ).build()

    @Provides
    fun providePendingMediaDao(db: AppDatabase): PendingMediaDao =
        db.pendingMediaDao()

    @Provides
    fun provideDispositivoDao(db: AppDatabase): DispositivoDao =
        db.dispositivoDao()

}