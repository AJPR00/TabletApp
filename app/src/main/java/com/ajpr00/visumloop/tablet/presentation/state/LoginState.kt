package com.ajpr00.visumloop.tablet.presentation.state

data class LoginState(
    val isLoggedIn: Boolean = false,
    val email: String? = null,
    val nombreCompleto: String? = null,
    val idToken: String? = null,
    val authCode: String? = null,
    val password: String = "",
    val showPassword: Boolean = false,
    val driveToken: String? = null
)
