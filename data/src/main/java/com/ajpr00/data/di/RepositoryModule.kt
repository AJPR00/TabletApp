package com.ajpr00.data.di

import com.ajpr00.core.domain.repository.login.StateAuthRepository
import com.ajpr00.core.domain.repository.network.DropboxRepository
import com.ajpr00.core.domain.repository.login.EmailAuthRepository
import com.ajpr00.core.domain.repository.login.FacebookAuthRepository
import com.ajpr00.core.domain.repository.network.FtpRepository
import com.ajpr00.core.domain.repository.login.GoogleAuthRepository
import com.ajpr00.data.repository.impl.login.AuthRepositoryImpl
import com.ajpr00.data.repository.impl.login.EmailAuthRepositoryImpl
import com.ajpr00.data.repository.impl.login.FacebookRepositoryImpl
import com.ajpr00.data.repository.impl.login.GoogleAuthRepositoryImpl
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

    // Auth
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): StateAuthRepository

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
    ): FacebookAuthRepository

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
