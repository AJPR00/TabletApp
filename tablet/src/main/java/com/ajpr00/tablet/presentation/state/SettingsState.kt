package com.ajpr00.tablet.presentation.state

data class SettingsState(
    val isDarkMode: Boolean = false,
    val language: String = "es",
    val tabletId: String = "",
    val tabletName: String = "",
    val isMobileConnected: Boolean = false,
    val mobileName: String = ""
)