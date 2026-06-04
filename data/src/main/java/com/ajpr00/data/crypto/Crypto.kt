package com.ajpr00.data.crypto

import android.util.Log
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class Crypto(userPlainKey: String) {

    companion object {
        private const val AES_KEY_SIZE = 16      // 128 bits
        private const val GCM_IV_SIZE = 12       // 96 bits
        private const val GCM_TAG_SIZE = 128     // bits del TAG
        private const val TAG = "AES"
    }

    // Convertimos la clave del usuario (texto plano) en clave AES real
    private val key: SecretKey = try {
        Log.d(TAG, "→ Derivando clave AES desde texto plano...")

        val keyBytes = MessageDigest.getInstance("SHA-256")
            .digest(userPlainKey.toByteArray())
            .copyOf(AES_KEY_SIZE)

        Log.d(TAG, "Clave AES generada (${keyBytes.size} bytes)")

        SecretKeySpec(keyBytes, "AES")
    } catch (e: Exception) {
        Log.e(TAG, "Error generando clave AES: ${e.message}")
        // Clave dummy para evitar nulls (no válida para descifrar)
        SecretKeySpec(ByteArray(AES_KEY_SIZE), "AES")
    }

    fun encrypt(plain: ByteArray): ByteArray {

        Log.d(TAG, "→ Iniciando cifrado...")

        return try {
            val iv = ByteArray(GCM_IV_SIZE)
            SecureRandom().nextBytes(iv)
            Log.d(TAG, "IV generado: ${iv.joinToString()}")

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(GCM_TAG_SIZE, iv)

            cipher.init(Cipher.ENCRYPT_MODE, key, spec)
            Log.d(TAG, "Cifrador inicializado con AES/GCM")

            val cipherText = cipher.doFinal(plain)
            Log.d(TAG, "Datos cifrados (${cipherText.size} bytes)")

            val finalData = iv + cipherText
            Log.d(TAG, "→ Cifrado final listo (IV + datos cifrados)")

            finalData

        } catch (e: Exception) {
            Log.e(TAG, "Error cifrando: ${e.message}")
            ByteArray(0)
        }
    }

    fun decrypt(encrypted: ByteArray): ByteArray {

        Log.d(TAG, "→ Iniciando descifrado...")

        return try {
            if (encrypted.size <= GCM_IV_SIZE) {
                Log.e(TAG, "Encrypted data too short")
                return ByteArray(0)
            }

            val iv = encrypted.copyOfRange(0, GCM_IV_SIZE)
            Log.d(TAG, "IV extraído: ${iv.joinToString()}")

            val cipherText = encrypted.copyOfRange(GCM_IV_SIZE, encrypted.size)
            Log.d(TAG, "CipherText extraído (${cipherText.size} bytes)")

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(GCM_TAG_SIZE, iv)

            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            Log.d(TAG, "Descifrador inicializado con AES/GCM")

            val plain = cipher.doFinal(cipherText)
            Log.d(TAG, "→ Descifrado completado (${plain.size} bytes)")

            plain

        } catch (e: AEADBadTagException) {
            Log.e(TAG, "TAG inválido: datos corruptos o clave incorrecta")
            ByteArray(0)
        } catch (e: Exception) {
            Log.e(TAG, "Error descifrando: ${e.message}")
            ByteArray(0)
        }
    }
}
