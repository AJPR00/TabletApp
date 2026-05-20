package com.ajpr00.core.domain.repository

import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.core.domain.model.MediaResult
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getAllMediaBd(): Flow<List<MediaContent>>
    suspend fun addBd(media: MediaContent)
    suspend fun deleteMedia(media: MediaContent)
    suspend fun listMediaFilesFTPFiltered(): List<MediaContent>
    suspend fun listMediaFilesDrive(): MediaResult
    suspend fun listMediaFilesLocalFiltered(localFiles: List<MediaContent>): List<MediaContent>
    fun getAllSync(): List<MediaContent>
    fun deleteSync(id: Int)
    fun getMediaById(id: Int): MediaContent?

}
