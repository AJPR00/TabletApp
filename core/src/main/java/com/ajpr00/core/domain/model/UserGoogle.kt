package com.ajpr00.core.domain.model

data class UserGoogle(
    val idToken: String?,
    val email: String? = "",
    val name: String? = "",
    val avatarUrl: String? = "",
    val authCode: String?
)
