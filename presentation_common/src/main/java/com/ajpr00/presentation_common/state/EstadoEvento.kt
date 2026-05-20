package com.ajpr00.presentation_common.state

sealed class EstadoEvento {
    object Exito : EstadoEvento()
    object Cargando : EstadoEvento()
    object Inicial : EstadoEvento()
    data class Mensajes(val mensajes: List<String>) : EstadoEvento()
}