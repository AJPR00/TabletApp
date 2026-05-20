package com.ajpr00.core.domain.repository

import com.ajpr00.core.domain.model.MediaContent

interface FtpRepository {
    suspend fun loginWithFTP(token: String): Result<Unit>
    suspend fun listMediaFiles(): List<MediaContent>
}
