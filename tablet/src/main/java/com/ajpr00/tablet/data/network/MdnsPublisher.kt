package com.ajpr00.tablet.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.InetAddress
import javax.inject.Inject
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

class MdnsPublisher @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var jmdns: JmDNS? = null
    private var multicastLock: WifiManager.MulticastLock? = null
    private val TAG = "MdnsPublisher"

    suspend fun start(id: String, name: String, port: Int) = withContext(Dispatchers.IO) {

        Log.d(TAG, "mDNS → Iniciando servicio mDNS para id=$id en puerto=$port")

        try {
            val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            multicastLock = wifi.createMulticastLock("visumloop-mdns").apply {
                setReferenceCounted(true)
                acquire()
            }
            Log.d(TAG, "mDNS → MulticastLock adquirido")

            val ip = getLocalIpAddress()
            Log.d(TAG, "mDNS → IP detectada de la tablet: $ip")

            jmdns = JmDNS.create(ip, "tablet-$id")
            Log.d(TAG, "mDNS → Instancia JmDNS creada correctamente")

            val serviceInfo = ServiceInfo.create(
                "_visumloop._tcp.local.",
                name,
                "",
                port,
                0,
                0,
                "id=$id"
            )
            jmdns?.registerService(serviceInfo)
            Log.d(TAG, "mDNS → Servicio registrado correctamente")

        } catch (e: Exception) {
            Log.e(TAG, "mDNS → Error al iniciar: ${e.message}", e)
        }
    }

    fun stop() {
        Log.d(TAG, "mDNS → Deteniendo servicio mDNS…")
        try {
            jmdns?.unregisterAllServices()
            jmdns?.close()
            multicastLock?.release()
            Log.d(TAG, "mDNS → Servicio detenido correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "mDNS → Error al detener: ${e.message}", e)
        }
    }

    private fun getLocalIpAddress(): InetAddress {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return InetAddress.getByName("0.0.0.0")
        val linkProps = cm.getLinkProperties(network) ?: return InetAddress.getByName("0.0.0.0")

        val ipv4 = linkProps.linkAddresses
            .map { it.address }
            .filterIsInstance<Inet4Address>()
            .firstOrNull()

        return ipv4 ?: InetAddress.getByName("0.0.0.0")
    }
}

