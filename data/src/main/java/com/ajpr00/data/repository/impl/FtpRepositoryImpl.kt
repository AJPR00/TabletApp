package com.ajpr00.data.repository.impl

import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.core.domain.repository.FtpRepository
import com.ajpr00.data.datasource.cloud.FtpClientDataSource
import com.ajpr00.data.datasource.local.preferences.SessionPreference
import javax.inject.Inject

class FtpRepositoryImpl @Inject constructor(
    private val dataSource: FtpClientDataSource,
    sessoinDS: SessionPreference
): FtpRepository {
    override suspend fun loginWithFTP(token: String): Result<Unit> {
        TODO("Not yet implemented")}

    override suspend fun listMediaFiles(): List<MediaContent> {
        TODO("Not yet implemented")
    }
}