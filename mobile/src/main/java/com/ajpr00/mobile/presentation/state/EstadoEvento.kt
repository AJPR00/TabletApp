package com.ajpr00.mobile.presentation.state

sealed class EstadoEvento {
    object Exito : EstadoEvento()
    object Cargando : EstadoEvento()
    object Inicial : EstadoEvento()
    data class Mensajes(val mensajes: List<String>) : EstadoEvento()
}