package com.ajpr00.tabletapp.domain.model

data class ReproductorConfig(
    val tiempoImagen: Long = 5000L,
    val transitionOption: TransitionOption = TransitionOption.Scale(1500, 2f),
    val isMuted: Boolean = false,
    val loopEnabled: Boolean = true,
    val isPlaying: Boolean = true,
    val rewinds: Int = 0,
    val volume: Float = 0.0f,
    val lastVolume: Float = 0.0f
)
