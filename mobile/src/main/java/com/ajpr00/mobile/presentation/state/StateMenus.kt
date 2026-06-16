package com.ajpr00.mobile.presentation.state

data class StateMenus(
    var showMenuReproductor: Boolean = false,
    var showMenuApp: Boolean = false,
    var showSidePanel: Boolean = false,
    var isLockScreen: Boolean = true,
    val lastInteraction: Long = System.currentTimeMillis())

