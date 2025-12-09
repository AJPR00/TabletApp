package com.ajpr00.visumloop.tablet.presentation.state

data class RegisterState(
    val email: String? = null,
    val confirmEmail: String? = null,
    val password: String = "",
    val confirmPassword: String = "",
    val showPassword: Boolean = false,
    val driveToken: String? = null,
    val idToken: String? = null,
    val loading: Boolean = false
)