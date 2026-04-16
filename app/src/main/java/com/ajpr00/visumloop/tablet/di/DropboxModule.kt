package com.ajpr00.visumloop.tablet.di

import com.ajpr00.visumloop.tablet.data.repository.DropboxRepository
import com.ajpr00.visumloop.tablet.data.repository.impl.DropboxRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DropboxModule {

    @Binds
    @Singleton
    abstract fun bindDropboxRepository(
        impl: DropboxRepositoryImpl
    ): DropboxRepository
}
