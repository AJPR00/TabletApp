package com.ajpr00.tablet.data.di

import com.ajpr00.core.domain.repository.media.MediaRepository
import com.ajpr00.core.domain.repository.media.PlaylistRepository
import com.ajpr00.core.domain.repository.network.NetworkRepository
import com.ajpr00.core.domain.repository.pairing.PairingRepository
import com.ajpr00.core.domain.repository.preference.SessionManager
import com.ajpr00.core.domain.repository.preference.SettingsManager
import com.ajpr00.data.repository.impl.network.NetworkRepositoryImpl
import com.ajpr00.tablet.data.repositoryImp.MediaRepositoryImpl
import com.ajpr00.tablet.data.repositoryImp.PairingRepositoryImpl
import com.ajpr00.tablet.data.repositoryImp.PlaylistRepositoryImpl
import com.ajpr00.tablet.data.repositoryImp.preference.TabletAppPreferencesRepositoryImpl
import com.ajpr00.tablet.data.repositoryImp.preference.TabletSessionPreferencesRepositoryImpl
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
        impl: TabletAppPreferencesRepositoryImpl
    ): SettingsManager

    @Binds
    @Singleton
    abstract fun bindPairingRepository(
        impl: PairingRepositoryImpl
    ): PairingRepository

    @Binds
    @Singleton
    abstract fun bindSessionManager(
        impl: TabletSessionPreferencesRepositoryImpl
    ): SessionManager

    @Binds
    @Singleton
    abstract fun bindNetworkRepository(
        impl: NetworkRepositoryImpl
    ): NetworkRepository
}

