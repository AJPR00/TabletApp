package com.ajpr00.visumloop.tablet.di

import com.ajpr00.visumloop.tablet.data.repository.FtpRepository
import com.ajpr00.visumloop.tablet.data.repository.impl.FtpRepositoryImpl
import com.ajpr00.visumloop.tablet.domain.model.FtpConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FtpModule {

    @Binds
    abstract fun bindFtpRepository(
        impl: FtpRepositoryImpl
    ): FtpRepository

    // Companion object (objeto acompañante)
    companion object {

        @Provides
        fun provideFtpConfig(): FtpConfig =
            FtpConfig("ftp.tuservidor.com", 21, "usuario", "password")
    }
}
