package com.ajpr00.core.domain.model.qr

data class QrPayload(
    val id: String,
    val nombre: String,
    val ip: String,
    val puerto: Int,
    val aesKey: String
)
