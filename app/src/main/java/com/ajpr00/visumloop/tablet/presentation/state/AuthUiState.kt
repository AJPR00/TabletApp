package com.ajpr00.visumloop.tablet.presentation.state

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val email: String? = null,
    val nombreCompleto: String? = null,
    val idToken: String? = null,
    val authCode: String? = null
)
