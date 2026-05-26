package com.ajpr00.core.domain.repository

import com.ajpr00.core.domain.model.MediaContent
import com.ajpr00.core.domain.model.MediaResult
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getAllMediaBd(): Flow<List<MediaContent>>
    suspend fun insertAll(medias: List<MediaContent>)
    suspend fun addBd(media: MediaContent)
    suspend fun deleteMedia(media: MediaContent)

    suspend fun getMediaById(id: String): MediaContent?

    suspend fun listMediaFilesFTPFiltered(): List<MediaContent>
    suspend fun listMediaFilesDrive(): MediaResult

    suspend fun listMediaFilesLocalFiltered(localFiles: List<MediaContent>): List<MediaContent>


    // Síncrono (NanoHTTPD)
    fun getAllSync(): List<MediaContent>
    fun deleteSync(id: Int)
    fun getMediaByIdSync(id: Int): MediaContent?

}

