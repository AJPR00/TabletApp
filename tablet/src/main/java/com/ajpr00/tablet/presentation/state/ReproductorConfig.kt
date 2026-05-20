package com.ajpr00.tablet.presentation.state

import com.ajpr00.tablet.presentation.transitions.TransitionOption

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