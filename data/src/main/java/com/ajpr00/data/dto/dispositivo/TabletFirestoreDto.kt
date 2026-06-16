package com.ajpr00.data.dto.dispositivo

data class TabletFirestoreDto(
    val id: String = "",
    val emailUsuario: String = "",
    val modelo: String = "",
    val ip: String = "",
    val puerto: Int = 0,
    val estado: String = "ONLINE",
    val ultimoUpdate: Long = 0
)
