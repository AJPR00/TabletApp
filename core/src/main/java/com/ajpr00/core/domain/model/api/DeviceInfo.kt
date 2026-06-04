package com.ajpr00.core.domain.model.api

data class DeviceInfo(
    val id: String,
    val token: String?=null,
    val name: String,
    val port: Int,
)
