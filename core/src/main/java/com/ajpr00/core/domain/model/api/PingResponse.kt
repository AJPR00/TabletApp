package com.ajpr00.core.domain.model.api

data class PingResponse(
    val status: String = "ok",
    val id: String,
    val name: String
)
