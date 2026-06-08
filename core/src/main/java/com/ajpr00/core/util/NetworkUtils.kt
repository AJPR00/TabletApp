package com.ajpr00.core.util

import java.net.Inet4Address
import java.net.NetworkInterface

object NetworkUtils {

    /**
     * Devuelve la IP completa del dispositivo (ej: 192.168.1.45)
     * Compatible Android 23 → 34.
     */
    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {

                if (!intf.isUp || intf.isLoopback) continue

                val name = intf.displayName.lowercase()

                // Ignorar VPN, tun, rmnet, p2p, etc.
                if (name.contains("tun") ||
                    name.contains("ppp") ||
                    name.contains("vpn") ||
                    name.contains("rmnet") ||
                    name.contains("p2p")) continue

                val addrs = intf.inetAddresses
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val ip = addr.hostAddress
                        return ip
                    }
                }
            }
        } catch (e: Exception) {
        }
        return null
    }

    /**
     * Devuelve la IP base (ej: 192.168.1)
     * Ahora usa getLocalIpAddress() para evitar duplicación.
     */
    fun getBaseIp(debugLog: ((String) -> Unit)? = null): String? {
        val ip = getLocalIpAddress() ?: run {
            debugLog?.invoke("No se pudo obtener la IP base.")
            return null
        }

        debugLog?.invoke("IP local detectada: $ip")

        val parts = ip.split(".")
        return if (parts.size == 4) {
            val base = "${parts[0]}.${parts[1]}.${parts[2]}"
            debugLog?.invoke("IP base: $base")
            base
        } else {
            debugLog?.invoke("Formato IP inválido.")
            null
        }
    }
}
