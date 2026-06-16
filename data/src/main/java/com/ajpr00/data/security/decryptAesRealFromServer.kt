package com.ajpr00.data.security

import android.util.Base64
import com.ajpr00.core.security.Crypto
import com.ajpr00.core.security.Pbkdf2KeyDeriver

fun decryptAesRealFromServer(pin: String, saltBase64: String, encryptedBase64: String): ByteArray {
    // 1. Decodificar salt
    val salt = Base64.decode(saltBase64, Base64.DEFAULT)

    // 2. Derivar clave temporal PBKDF2
    val tempKeyBytes = Pbkdf2KeyDeriver.deriveKeyFromPin(pin, salt)

    // 3. Crear Crypto con la clave temporal
    val crypto = Crypto(tempKeyBytes)

    // 4. Decodificar paquete cifrado
    val encrypted = Base64.decode(encryptedBase64, Base64.DEFAULT)

    // 5. Descifrar AES_REAL
    return crypto.decrypt(encrypted)
}
