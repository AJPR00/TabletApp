package com.ajpr00.tablet.data.datasource.local.db.dao

import androidx.room.*
import com.ajpr00.tablet.data.datasource.local.db.entity.MediaContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaContentDao {
    @Query("SELECT * FROM media_content")
    fun getAllMedia(): Flow<List<MediaContentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: MediaContentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<MediaContentEntity>)

    @Update
    suspend fun update(media: MediaContentEntity)

    @Delete
    suspend fun delete(media: MediaContentEntity)

    @Query("SELECT * FROM media_content WHERE id = :id")
    suspend fun getById(id: String): MediaContentEntity?

    @Query("SELECT * FROM media_content")
    suspend fun getAllOnce(): List<MediaContentEntity>

    @Query("DELETE FROM media_content WHERE id = :id")
    suspend fun deleteById(id: String)

}
