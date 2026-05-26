package com.ajpr00.tablet.data.di

import android.content.Context
import androidx.room.Room
import com.ajpr00.tablet.data.datasource.local.MediaLocalDataSource
import com.ajpr00.tablet.data.datasource.local.MediaLocalDataSourceImpl
import com.ajpr00.tablet.data.datasource.local.PlaylistLocalDataSource
import com.ajpr00.tablet.data.datasource.local.PlaylistLocalDataSourceImpl
import com.ajpr00.tablet.data.datasource.local.db.AppDatabase
import com.ajpr00.tablet.data.datasource.local.db.dao.MediaContentDao
import com.ajpr00.tablet.data.datasource.local.db.dao.PlaylistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TabletDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "tablet_media_db"
        ).build()

    @Provides
    fun provideMediaContentDao(db: AppDatabase): MediaContentDao =
        db.mediaContentDao()

    @Provides
    fun providePlaylistDao(db: AppDatabase): PlaylistDao =
        db.playlistDao()


    @Module
    @InstallIn(SingletonComponent::class)
    object TabletLocalDataSourceModule {

        @Provides
        @Singleton
        fun provideMediaLocalDataSource(
            daoMedia: MediaContentDao,
            daoPlaylist: PlaylistDao
        ): MediaLocalDataSource =
            MediaLocalDataSourceImpl(daoMedia, daoPlaylist)

        @Provides
        fun providePlaylistLocalDataSource(
            dao: PlaylistDao
        ): PlaylistLocalDataSource =
            PlaylistLocalDataSourceImpl(dao)
    }
}
