package com.ajpr00.data.di

import com.ajpr00.core.domain.model.GoogleConfig
import com.ajpr00.data.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GoogleConfigModule {

    @Provides
    @Singleton
    fun provideGoogleConfig(): GoogleConfig {
        return GoogleConfig(
            clientId = BuildConfig.GOOGLE_CLIENT_ID,
            clientSecret = BuildConfig.GOOGLE_CLIENT_SECRET,
            redirectUri = BuildConfig.GOOGLE_REDIRECT_URI
        )
    }
}
