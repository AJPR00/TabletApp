package com.ajpr00.visumloop.tablet.di

import com.ajpr00.visumloop.tablet.data.repository.EmailAuthRepository
import com.ajpr00.visumloop.tablet.data.repository.impl.EmailAuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EmailAuthModule {

    @Binds
    @Singleton
    abstract fun bindEmailAuthRepository(
        impl: EmailAuthRepositoryImpl
    ): EmailAuthRepository
}
