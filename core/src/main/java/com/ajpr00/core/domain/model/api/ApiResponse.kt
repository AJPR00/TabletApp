package com.ajpr00.core.domain.model.api

data class ApiResponse<T>(
    val status: String,
    val data: T? = null,
    val error: String? = null
)