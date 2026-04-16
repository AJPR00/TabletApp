package com.ajpr00.visumloop.tablet.data.repository.impl

import com.ajpr00.visumloop.tablet.data.datasource.cloud.FtpClientDataSource
import com.ajpr00.visumloop.tablet.data.datasource.local.preferences.SessionPreference
import com.ajpr00.visumloop.tablet.data.repository.FtpRepository
import com.ajpr00.visumloop.tablet.domain.model.MediaContent
import javax.inject.Inject

class FtpRepositoryImpl @Inject constructor(
    private val dataSource: FtpClientDataSource,
    sessoinDS: SessionPreference
): FtpRepository{
    override suspend fun loginWithFTP(token: String): Result<Unit> {
        TODO("Not yet implemented")}

    override suspend fun listMediaFiles(): List<MediaContent> {
        TODO("Not yet implemented")
    }
}