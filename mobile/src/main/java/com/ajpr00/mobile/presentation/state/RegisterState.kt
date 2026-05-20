package com.ajpr00.mobile.presentation.state

data class RegisterState(
    val email: String = "",
    val confirmEmail: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val showPassword: Boolean = false,
    val driveToken: String? = null,
    val idToken: String? = null,
    val loading: Boolean = false
)