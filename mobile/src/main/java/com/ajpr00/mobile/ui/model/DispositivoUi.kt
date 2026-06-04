package com.ajpr00.mobile.ui.model

import com.ajpr00.core.domain.model.EstadoDispositivo
data class DispositivoUi(
    val id: String,
    val ip: String? = null,
    val puerto: Int? = null,
    val nombre: String,
    val nivelBatery: Int? = null,
    val icono: Int,
    val estado: EstadoDispositivo,
)
