package com.ajpr00.tablet.data.di

import com.ajpr00.core.domain.usecase.media.DeleteMediaSyncUseCase
import com.ajpr00.core.domain.usecase.media.GetAllMediaSyncUseCase
import com.ajpr00.core.domain.usecase.media.GetMediaByIdSyncUseCase
import com.ajpr00.data.useCase.ImportMediaFromServerUseCase
import com.ajpr00.tablet.data.server.TabletServer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServerModule {

    @Provides
    @Singleton
    fun provideTabletServer(
        getMediaByIdSync: GetMediaByIdSyncUseCase,
        deleteMediaSyncUseCase: DeleteMediaSyncUseCase,
        getAllMediaSyncUseCase: GetAllMediaSyncUseCase,
        importMediaFromServerUseCase: ImportMediaFromServerUseCase
    ): TabletServer {
        return TabletServer(
            getMediaByIdSync,
            deleteMediaSyncUseCase,
            getAllMediaSyncUseCase,
            importMediaFromServerUseCase
        )
    }
}

