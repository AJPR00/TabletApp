package com.ajpr00.tablet.data.datasource.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa un archivo multimedia almacenado en la tablet.
 *
 * Cada media tiene:
 *  - `id`: identificador único (UUID generado en dominio).
 *  - `name`: nombre del archivo.
 *  - `path`: ruta absoluta en el almacenamiento local.
 *  - `type`: tipo de archivo (imagen, vídeo, audio…).
 *
 * La columna `path` está indexada y es única para evitar duplicados.
 */
@Entity(
    tableName = "media_content",
    indices = [
        Index(value = ["path"], unique = true)
    ]
)
data class MediaContentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val path: String,
    val type: String,
)
