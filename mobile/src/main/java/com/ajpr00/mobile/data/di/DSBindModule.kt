package com.ajpr00.mobile.data.di

import com.ajpr00.mobile.data.datasource.local.DispositivoLocalDataSource
import com.ajpr00.mobile.data.datasource.local.DispositivoLocalDataSourceImpl
import com.ajpr00.mobile.data.datasource.local.PendingMediaLocalDataSource
import com.ajpr00.mobile.data.datasource.local.PendingMediaLocalDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DSBindModule {

    @Binds
    @Singleton
    abstract fun bindPendingMediaLocalDataSource(
        impl: PendingMediaLocalDataSourceImpl
    ): PendingMediaLocalDataSource

    @Binds
    @Singleton
    abstract fun bindDispositivoLocalDataSource(
        impl: DispositivoLocalDataSourceImpl
    ): DispositivoLocalDataSource

}


