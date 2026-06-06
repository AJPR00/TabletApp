package com.ajpr00.data.datasource.network

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.util.Log
import androidx.annotation.RequiresPermission
import com.ajpr00.core.domain.model.mDNS.MdnsServiceInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.InetAddress
import javax.inject.Inject
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener

class MdnsResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val TAG = "MdnsResolver"

    private var jmdns: JmDNS? = null
    private var multicastLock: WifiManager.MulticastLock? = null

    fun discover(serviceType: String = "_visumloop._tcp.local."): Flow<MdnsServiceInfo> = callbackFlow {

        Log.d(TAG, "discover() → Iniciando descubrimiento mDNS para tipo: $serviceType")

        val job = launch(Dispatchers.IO) {

            try {
                val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

                Log.d(TAG, "discover() → Creando MulticastLock…")
                multicastLock = wifi.createMulticastLock("mdns_lock").apply {
                    setReferenceCounted(true)
                    acquire()
                }
                Log.d(TAG, "discover() → MulticastLock adquirido correctamente")

                // IP REAL usando API moderna
                val ip = getLocalIpAddress()
                Log.d(TAG, "discover() → IP local detectada (API moderna): $ip")

                Log.d(TAG, "discover() → Creando instancia JmDNS…")
                jmdns = JmDNS.create(ip)
                Log.d(TAG, "discover() → JmDNS creado correctamente")

                val listener = object : ServiceListener {

                    override fun serviceAdded(event: ServiceEvent) {
                        Log.d(TAG, "mDNS → serviceAdded: ${event.name}")
                        jmdns?.getServiceInfo(event.type, event.name)
                    }

                    override fun serviceRemoved(event: ServiceEvent) {
                        Log.d(TAG, "mDNS → serviceRemoved: ${event.name}")
                    }

                    override fun serviceResolved(event: ServiceEvent) {
                        Log.d(TAG, "mDNS → serviceResolved: ${event.info}")

                        val info = MdnsServiceInfo(
                            name = event.info.name,
                            ip = event.info.inet4Addresses.firstOrNull()?.hostAddress ?: "",
                            port = event.info.port,
                            txt = event.info.textString
                        )

                        Log.d(TAG, "mDNS → Tablet detectada: $info")
                        trySend(info)
                    }
                }

                Log.d(TAG, "discover() → Registrando listener mDNS…")
                jmdns?.addServiceListener(serviceType, listener)
                Log.d(TAG, "discover() → Listener registrado correctamente")

            } catch (e: Exception) {
                Log.e(TAG, "discover() → ERROR: ${e.message}", e)
            }
        }

        awaitClose {
            Log.d(TAG, "discover() → Cerrando flujo mDNS…")

            try {
                job.cancel()
                Log.d(TAG, "discover() → Job cancelado")

                jmdns?.close()
                Log.d(TAG, "discover() → JmDNS cerrado")

                multicastLock?.release()
                Log.d(TAG, "discover() → MulticastLock liberado")

            } catch (e: Exception) {
                Log.e(TAG, "discover() → ERROR al cerrar: ${e.message}", e)
            }

            Log.d(TAG, "discover() → Cierre completo")
        }
    }

    /**
     * ⭐ Obtiene la IP REAL del móvil usando la API moderna (Android 10–14)
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun getLocalIpAddress(): InetAddress {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return InetAddress.getByName("0.0.0.0")
            val linkProps = cm.getLinkProperties(network) ?: return InetAddress.getByName("0.0.0.0")

            val ipv4 = linkProps.linkAddresses
                .map { it.address }
                .filterIsInstance<Inet4Address>()
                .firstOrNull()

            ipv4 ?: InetAddress.getByName("0.0.0.0")

        } catch (e: Exception) {
            Log.e(TAG, "getLocalIpAddress() → ERROR: ${e.message}")
            InetAddress.getByName("0.0.0.0")
        }
    }

    private var discoveryJob: Job? = null

    fun startDiscovery(
        serviceType: String = "_visumloop._tcp.local.",
        onDeviceFound: (MdnsServiceInfo) -> Unit
    ) {
        // Evitar múltiples búsquedas simultáneas
        if (discoveryJob?.isActive == true) return

        discoveryJob = CoroutineScope(Dispatchers.IO).launch {
            discover(serviceType).collect { info ->
                onDeviceFound(info)
            }
        }
    }

    fun stopDiscovery() {
        try {
            discoveryJob?.cancel()
            discoveryJob = null

            jmdns?.close()
            jmdns = null

            multicastLock?.release()
            multicastLock = null

            Log.d(TAG, "stopDiscovery() → Búsqueda mDNS detenida correctamente")

        } catch (e: Exception) {
            Log.e(TAG, "stopDiscovery() → ERROR: ${e.message}")
        }
    }

}