package com.ajpr00.mobile.presentation.state

data class SettingsState(
    val isDarkMode: Boolean = false,
    val language: String = "es",
    val movilId: String = "",
    val movilName: String = "",
    val isRedConnected: Boolean = false,
)