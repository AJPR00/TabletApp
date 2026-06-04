package com.ajpr00.mobile.data.di

import com.ajpr00.core.domain.repository.dispositivo.DispositivoRepository
import com.ajpr00.core.domain.repository.media.PendingMediaRepository
import com.ajpr00.core.domain.repository.dispositivo.TabletLocatorRepository
import com.ajpr00.core.domain.repository.login.AuthPreference
import com.ajpr00.core.domain.repository.media.TabletApiRepository
import com.ajpr00.core.domain.repository.preference.PreferencesRepository
import com.ajpr00.data.repository.impl.dispositivo.TabletLocatorRepositoryImpl
import com.ajpr00.mobile.data.repositoryImp.DispositivoRepositoryImpl
import com.ajpr00.mobile.data.repositoryImp.MobileAuthPreferenceImpl
import com.ajpr00.mobile.data.repositoryImp.PendingMediaRepositoryImpl
import com.ajpr00.mobile.data.repositoryImp.PreferencesRepositoryImpl
import com.ajpr00.mobile.data.repositoryImp.TabletApiRepositoryImpl
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
    abstract fun bindDispositivoRepository(
        impl: DispositivoRepositoryImpl
    ): DispositivoRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        impl: PreferencesRepositoryImpl
    ): PreferencesRepository

    @Binds
    @Singleton
    abstract fun bindPendingMediaRepository(
        impl: PendingMediaRepositoryImpl
    ): PendingMediaRepository

    @Binds
    abstract fun bindTabletLocatorRepository(
        impl: TabletLocatorRepositoryImpl
    ): TabletLocatorRepository

    @Binds
    @Singleton
    abstract fun bindTabletRepository(
        impl: TabletApiRepositoryImpl
    ): TabletApiRepository

    @Binds
    abstract fun bindAuthPreference(impl: MobileAuthPreferenceImpl): AuthPreference
}
