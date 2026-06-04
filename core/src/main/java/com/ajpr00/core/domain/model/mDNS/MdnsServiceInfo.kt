package com.ajpr00.core.domain.model.mDNS

data class MdnsServiceInfo(
    val name: String,
    val ip: String,
    val port: Int,
    val txt: String
)