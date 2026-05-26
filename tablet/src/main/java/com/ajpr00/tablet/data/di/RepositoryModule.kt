package com.ajpr00.tablet.data.di

import com.ajpr00.core.domain.repository.MediaRepository
import com.ajpr00.core.domain.repository.PlaylistRepository
import com.ajpr00.tablet.data.repositoryImp.MediaRepositoryImpl
import com.ajpr00.tablet.data.repositoryImp.PlaylistRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMediaRepository(
        impl: MediaRepositoryImpl
    ): MediaRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(
        impl: PlaylistRepositoryImpl
    ): PlaylistRepository
}

