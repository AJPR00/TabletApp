package com.ajpr00.data.di

import com.ajpr00.core.domain.model.FtpConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FtpModule {

    @Provides
    @Singleton
    fun provideFtpConfig(): FtpConfig =
        FtpConfig(
            host = "ftp.tuservidor.com",
            port = 21,
            user = "usuario",
            password = "password"
        )
}
