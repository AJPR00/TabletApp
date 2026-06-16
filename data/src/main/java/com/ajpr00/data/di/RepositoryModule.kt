package com.ajpr00.data.di

import com.ajpr00.core.domain.repository.login.AuthRepository
import com.ajpr00.data.repository.network.DropboxRepository
import com.ajpr00.data.repository.network.FtpRepository
import com.ajpr00.data.repository.impl.login.AuthRepositoryImpl
import com.ajpr00.data.repository.impl.network.DropboxRepositoryImpl
import com.ajpr00.data.repository.impl.network.FtpRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    //Facebook
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    //Dropbox
    @Binds
    @Singleton
    abstract fun bindDropboxRepository(
        impl: DropboxRepositoryImpl
    ): DropboxRepository

    //FTP
    @Binds
    @Singleton
    abstract fun bindFtpRepository(
        impl: FtpRepositoryImpl
    ): FtpRepository

}
