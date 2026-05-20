package com.ajpr00.presentation_common.state

data class LoginState(
    val isLoggedLocal: Boolean = false,
    val isLoggedDrive: Boolean = false,
    val email: String? = null,
    val nombreCompleto: String? = null,
    val idToken: String? = null,
    val authCode: String? = null,
    val password: String = "",
    val showPassword: Boolean = false,
    val driveToken: String? = null,
    val loading: Boolean = false,
    val imageUrl: String? = null
)
