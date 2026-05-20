package com.ajpr00.mobile.data.datasource.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dispositivos")
data class DispositivoEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val ip: String?,
    val puerto: Int?,
    val tipo: String,
    val token: String
)
