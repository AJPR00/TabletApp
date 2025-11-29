package com.ajpr00.tabletapp.data.db.dao

import androidx.room.*
import com.ajpr00.tabletapp.domain.model.MediaContent

@Dao
interface MediaContentDao {
    @Query("SELECT * FROM media_content")
    suspend fun getAll(): List<MediaContent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: MediaContent)

    @Update
    suspend fun update(media: MediaContent)

    @Delete
    suspend fun delete(media: MediaContent)
}
