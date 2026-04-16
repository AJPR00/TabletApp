package com.ajpr00.visumloop.tablet.data.repository;

import com.ajpr00.visumloop.tablet.domain.model.MediaContent

interface FtpRepository {
    suspend fun loginWithFTP(token: String): Result<Unit>
    suspend fun listMediaFiles(): List<MediaContent>
}
