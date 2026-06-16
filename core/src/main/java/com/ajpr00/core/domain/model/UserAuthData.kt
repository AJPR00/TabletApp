package com.ajpr00.core.domain.model

data class UserAuthData(
    val username: String?,
    val email: String,
    val avatarUrl: String,
    val token: String?,
    val authCode: String?= null
)
