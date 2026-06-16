package com.ajpr00.tablet.data.di

import com.facebook.CallbackManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object FacebookModule {

    @Provides
    fun provideCallbackManager(): CallbackManager =
        CallbackManager.Factory.create()
}
