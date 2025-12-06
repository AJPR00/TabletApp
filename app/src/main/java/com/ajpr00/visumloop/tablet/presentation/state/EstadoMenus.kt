package com.ajpr00.visumloop.tablet.presentation.state

data class EstadoMenus(
    val showMenuReproductor: Boolean = false,
    val showMenuApp: Boolean = false,
    val showSidePanel: Boolean = false,
    val onLockScreen: Boolean = true,
)

