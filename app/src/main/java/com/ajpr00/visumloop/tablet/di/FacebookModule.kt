package com.ajpr00.visumloop.tablet.di

import com.ajpr00.visumloop.tablet.data.repository.FacebookRepository
import com.ajpr00.visumloop.tablet.data.repository.impl.FacebookRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FacebookModule {

    @Binds
    @Singleton
    abstract fun bindFacebookRepository(
        impl: FacebookRepositoryImpl
    ): FacebookRepository
}
