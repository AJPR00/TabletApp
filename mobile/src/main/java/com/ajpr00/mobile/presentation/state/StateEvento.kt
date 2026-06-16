package com.ajpr00.mobile.presentation.state

sealed class StateEvento {
    object Exito : StateEvento()
    object Cargando : StateEvento()
    object Inicial : StateEvento()
    data class Mensajes(val mensajes: List<String>) : StateEvento()
}