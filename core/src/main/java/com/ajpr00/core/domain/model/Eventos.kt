package com.ajpr00.core.domain.model

sealed class Eventos {
    data object RegisterSuccess : Eventos()
    data class Error(val mensaje: String) : Eventos()
    data class Info(val mensaje: String) : Eventos()
}
