package com.ajpr00.core.util

import java.net.Inet4Address
import java.net.NetworkInterface
import kotlin.collections.iterator

object NetworkUtils {

    fun getBaseIp(debugLog: ((String) -> Unit)? = null): String? {
        debugLog?.invoke("Buscando IP base...")

        val interfaces = NetworkInterface.getNetworkInterfaces()
        for (interfaz in interfaces) {
            val addrs = interfaz.inetAddresses
            for (addr in addrs) {
                if (!addr.isLoopbackAddress && addr is Inet4Address) {
                    val ip = addr.hostAddress
                    debugLog?.invoke("IP local detectada: $ip")

                    val parts = ip.split(".")
                    if (parts.size == 4) {
                        val base = "${parts[0]}.${parts[1]}.${parts[2]}"
                        debugLog?.invoke("IP base: $base")
                        return base
                    }
                }
            }
        }

        debugLog?.invoke("No se pudo obtener la IP base.")
        return null
    }
}
