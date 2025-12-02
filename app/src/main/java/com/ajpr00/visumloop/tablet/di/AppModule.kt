package com.ajpr00.visumloop.tablet.di

import android.content.Context
import com.ajpr00.visumloop.tablet.R
import com.ajpr00.visumloop.tablet.data.repository.GoogleDriveRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideClientId(@ApplicationContext context: Context): String {
        return context.getString(R.string.web_client_id)
    }

    @Provides
    @Singleton
    fun provideGoogleDriveRepository(@ApplicationContext context: Context): GoogleDriveRepository =
        GoogleDriveRepository(
            clientId = context.getString(R.string.web_client_id),
            clientSecret = context.getString(R.string.web_client_id)
        )

}
