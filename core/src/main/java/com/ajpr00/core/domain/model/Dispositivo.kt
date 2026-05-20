package com.ajpr00.core.domain.model

data class Dispositivo(
    val id: String,
    val nombre: String = "Desconocido",
    val ip: String? = null,
    val puerto: Int? = null,
    val nivelBatery: Int? = null,
    val estado: EstadoDispositivo = EstadoDispositivo.DESCONOCIDO
)

enum class EstadoDispositivo {
    ONLINE, OFFLINE, DESCONOCIDO
}