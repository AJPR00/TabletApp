package com.ajpr00.visumloop.tablet.data.repository

import com.ajpr00.visumloop.tablet.domain.model.MediaContent
import com.ajpr00.visumloop.tablet.domain.model.MediaResult
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getAllMediaBd(): Flow<List<MediaContent>>
    suspend fun addBd(media: MediaContent)
    suspend fun deleteMedia(media: MediaContent)
    suspend fun listMediaFilesFTPFiltered(): List<MediaContent>
    suspend fun listMediaFilesDrive(): MediaResult
    suspend fun listMediaFilesLocalFiltered(localFiles: List<MediaContent>): List<MediaContent>
}
