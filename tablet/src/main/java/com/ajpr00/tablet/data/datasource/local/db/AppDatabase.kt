package com.ajpr00.tablet.data.datasource.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ajpr00.tablet.data.datasource.local.db.dao.MediaContentDao
import com.ajpr00.tablet.data.datasource.local.db.dao.PlaylistDao
import com.ajpr00.tablet.data.datasource.local.db.entity.MediaContentEntity
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistEntity
import com.ajpr00.tablet.data.datasource.local.db.entity.PlaylistMediaCrossRef

/**
 * Base de datos local de la tablet.
 *
 * Contiene las tablas principales relacionadas con la gestión multimedia:
 *  - `media_content`: archivos multimedia almacenados en la tablet.
 *  - `playlist`: listas de reproducción creadas por el usuario.
 *  - `playlist_media`: tabla intermedia many-to-many entre playlists y medias.
 *
 * Room genera automáticamente las implementaciones de los DAOs declarados.
 * Esta base de datos es el punto central de acceso a la persistencia local.
 */
@Database(
    entities = [
        MediaContentEntity::class,
        PlaylistEntity::class,
        PlaylistMediaCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** DAO para operaciones CRUD sobre archivos multimedia. */
    abstract fun mediaContentDao(): MediaContentDao

    /** DAO para operaciones CRUD sobre playlists y sus relaciones. */
    abstract fun playlistDao(): PlaylistDao
}
