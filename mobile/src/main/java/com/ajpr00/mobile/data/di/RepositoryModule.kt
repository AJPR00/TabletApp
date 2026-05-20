package com.ajpr00.mobile.data.di

import com.ajpr00.core.domain.repository.DispositivoRepository
import com.ajpr00.core.domain.repository.PendingMediaRepository
import com.ajpr00.mobile.data.repositoryImp.DispositivoRepositoryImpl
import com.ajpr00.mobile.data.repositoryImp.PendingMediaRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DispositivoRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDispositivoRepository(
        impl: DispositivoRepositoryImpl
    ): DispositivoRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PendingMediaRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPendingMediaRepository(
        impl: PendingMediaRepositoryImpl
    ): PendingMediaRepository
}
