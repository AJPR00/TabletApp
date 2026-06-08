package com.ajpr00.tablet.data.di

import com.ajpr00.core.domain.repository.login.AuthPreference
import com.ajpr00.core.domain.repository.media.MediaRepository
import com.ajpr00.core.domain.repository.media.PlaylistRepository
import com.ajpr00.core.domain.repository.pairing.PairingRepository
import com.ajpr00.core.domain.repository.preference.PreferencesRepository
import com.ajpr00.tablet.data.repositoryImp.MediaRepositoryImpl
import com.ajpr00.tablet.data.repositoryImp.PairingRepositoryImpl
import com.ajpr00.tablet.data.repositoryImp.PlaylistRepositoryImpl
import com.ajpr00.tablet.data.repositoryImp.PreferencesRepositoryImpl
import com.ajpr00.tablet.data.repositoryImp.TabletAuthPreferenceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMediaRepository(
        impl: MediaRepositoryImpl
    ): MediaRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(
        impl: PlaylistRepositoryImpl
    ): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        impl: PreferencesRepositoryImpl
    ): PreferencesRepository

    @Binds
    @Singleton
    abstract fun bindPairingRepository(
        impl: PairingRepositoryImpl
    ): PairingRepository

    @Binds
    abstract fun bindAuthPreference(impl: TabletAuthPreferenceImpl): AuthPreference
}

