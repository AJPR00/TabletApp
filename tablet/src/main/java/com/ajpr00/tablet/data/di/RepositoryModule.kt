package com.ajpr00.tablet.data.di

import com.ajpr00.core.domain.repository.MediaRepository
import com.ajpr00.tablet.data.repositoryImp.MediaRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMediaRepository(
        impl: MediaRepositoryImpl
    ): MediaRepository = impl
}

