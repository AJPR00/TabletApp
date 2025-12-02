package com.ajpr00.visumloop.tablet.di

import android.content.Context
import androidx.room.Room
import com.ajpr00.visumloop.tablet.data.db.local.AppDatabase
import com.ajpr00.visumloop.tablet.data.db.local.dao.MediaContentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "media_database"
        ).build()

    @Provides
    fun provideMediaContentDao(db: AppDatabase): MediaContentDao =
        db.mediaContentDao()
}
