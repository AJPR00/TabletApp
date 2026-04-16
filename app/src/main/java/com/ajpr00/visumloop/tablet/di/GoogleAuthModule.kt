package com.ajpr00.visumloop.tablet.di

import com.ajpr00.visumloop.tablet.data.repository.GoogleAuthRepository
import com.ajpr00.visumloop.tablet.data.repository.impl.GoogleAuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GoogleAuthModule {

    @Binds
    @Singleton
    abstract fun bindGoogleAuthRepository(
        impl: GoogleAuthRepositoryImpl
    ): GoogleAuthRepository
}
