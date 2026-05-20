package com.ajpr00.tablet.data.datasource.local.db.dao

import androidx.room.*
import com.ajpr00.tablet.data.datasource.local.db.entity.MediaContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaContentDao {
    @Query("SELECT * FROM media_content")
    fun getAllMedia(): Flow<List<MediaContentEntity>>

    /*@Query("SELECT * FROM media_content")
    suspend fun getAll(): List<MediaContentEntity>*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: MediaContentEntity)

    @Update
    suspend fun update(media: MediaContentEntity)

    @Delete
    suspend fun delete(media: MediaContentEntity)

    @Query("SELECT * FROM media_content WHERE id = :id")
    suspend fun getById(id: Int): MediaContentEntity?
}
