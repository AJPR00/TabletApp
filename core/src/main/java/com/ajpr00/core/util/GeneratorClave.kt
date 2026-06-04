package com.ajpr00.core.util

import java.security.SecureRandom

fun main() {
    val keyBytes = ByteArray(16)
    SecureRandom().nextBytes(keyBytes)

    print("Clave generada: ")
    keyBytes.forEach { print("0x" + it.toUByte().toString(16).uppercase() + ", ") }
}
