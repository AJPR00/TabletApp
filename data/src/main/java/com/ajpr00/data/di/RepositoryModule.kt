package com.ajpr00.data.di

import com.ajpr00.core.domain.repository.AuthRepository
import com.ajpr00.core.domain.repository.DropboxRepository
import com.ajpr00.core.domain.repository.EmailAuthRepository
import com.ajpr00.core.domain.repository.FacebookRepository
import com.ajpr00.core.domain.repository.FtpRepository
import com.ajpr00.core.domain.repository.GoogleAuthRepository
import com.ajpr00.data.repository.impl.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // Auth
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    //Email Auth
    @Binds
    @Singleton
    abstract fun bindEmailAuthRepository(
        impl: EmailAuthRepositoryImpl
    ): EmailAuthRepository

    //Facebook
    @Binds
    @Singleton
    abstract fun bindFacebookRepository(
        impl: FacebookRepositoryImpl
    ): FacebookRepository

    //Google Auth
    @Binds
    @Singleton
    abstract fun bindGoogleAuthRepository(
        impl: GoogleAuthRepositoryImpl
    ): GoogleAuthRepository

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
