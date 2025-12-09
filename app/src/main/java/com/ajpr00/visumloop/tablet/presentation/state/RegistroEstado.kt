package com.ajpr00.visumloop.tablet.presentation.state

sealed class RegistroEstado {
    object Exito : RegistroEstado()
    object Cargando : RegistroEstado()
    object Inicial : RegistroEstado()

    data class Error(val mensajes: List<String>) : RegistroEstado()
}