package com.ajpr00.visumloop.tablet.di

import com.ajpr00.visumloop.tablet.domain.model.FtpConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object FtpModule {
    @Provides
    fun provideFtpConfig(): FtpConfig =
        FtpConfig("ftp.tuservidor.com", 21, "usuario", "password")
}
