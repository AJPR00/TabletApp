package com.ajpr00.tablet.data.network

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo
import java.net.InetAddress

class MdnsPublisher(
    private val context: Context
) {

    // Aquí guardamos la instancia de JmDNS (el que se encarga de anunciar el servicio)
    private var jmdns: JmDNS? = null

    // El MulticastLock es obligatorio para que Android permita enviar/recibir paquetes mDNS
    private var multicastLock: WifiManager.MulticastLock? = null

    private val TAG = "MdnsPublisher"

    /**
     * start()
     * ---------------------------------------------------------
     * Esto arranca el anuncio mDNS.
     * Básicamente es como decirle a la red:
     * "¡Eh! Soy la tablet, estoy aquí, en esta IP y en este puerto".
     *
     * El móvil podrá descubrirnos sin saber la IP.
     */
    fun start(id: String,name: String, port: Int) {
        Log.d(TAG, "mDNS → Iniciando servicio mDNS para id=$id en puerto=$port")

        try {
            // 1) Pillamos el WifiManager para poder activar el MulticastLock
            val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            // 2) Activamos el MulticastLock (sin esto mDNS NO funciona en Android)
            multicastLock = wifi.createMulticastLock("visumloop-mdns")
            multicastLock?.acquire()
            Log.d(TAG, "mDNS → MulticastLock adquirido (necesario para mDNS)")

            // 3) Obtenemos la IP local del dispositivo (la tablet)
            val ip = intToInetAddress(wifi.connectionInfo.ipAddress)
            Log.d(TAG, "mDNS → IP detectada de la tablet: $ip")

            // 4) Creamos la instancia de JmDNS usando esa IP
            jmdns = JmDNS.create(ip, "tablet-$id")
            Log.d(TAG, "mDNS → Instancia JmDNS creada correctamente")

            // 5) Creamos la info del servicio que vamos a anunciar
            val serviceInfo = ServiceInfo.create(
                "_visumloop._tcp.local.",   // Tipo de servicio (como Chromecast, AirPlay, etc.)
                name,               // Nombre del dispositivo en la red
                port,                       // Puerto donde escucha NanoHTTPD
                "id=$id"                    // TXT record (info adicional)
            )

            // 6) Registramos el servicio → aquí es cuando la tablet "se anuncia"
            jmdns?.registerService(serviceInfo)
            Log.d(TAG, "mDNS → Servicio registrado en $ip:$port con id=$id")

        } catch (e: Exception) {
            Log.e(TAG, "mDNS → Error al iniciar: ${e.message}")
        }
    }

    /**
     * stop()
     * ---------------------------------------------------------
     * Esto apaga el anuncio mDNS.
     * Se llama cuando la app se cierra o cuando el servidor se detiene.
     */
    fun stop() {
        Log.d(TAG, "mDNS → Deteniendo servicio mDNS…")

        try {
            // Quitamos el servicio de la red
            jmdns?.unregisterAllServices()
            Log.d(TAG, "mDNS → Servicios mDNS desregistrados")

            // Cerramos JmDNS
            jmdns?.close()
            Log.d(TAG, "mDNS → Instancia JmDNS cerrada")

            // Liberamos el MulticastLock
            multicastLock?.release()
            Log.d(TAG, "mDNS → MulticastLock liberado")

        } catch (e: Exception) {
            Log.e(TAG, "mDNS → Error al detener: ${e.message}")
        }
    }

    /**
     * Convierte la IP que da Android (un Int) a una InetAddress válida.
     * Esto es un clásico en Android: la IP viene al revés y hay que recomponerla.
     */
    private fun intToInetAddress(ip: Int): InetAddress {
        val bytes = byteArrayOf(
            (ip and 0xff).toByte(),
            (ip shr 8 and 0xff).toByte(),
            (ip shr 16 and 0xff).toByte(),
            (ip shr 24 and 0xff).toByte()
        )

        Log.d(TAG, "mDNS → Convirtiendo IP int=$ip a InetAddress=${InetAddress.getByAddress(bytes)}")

        return InetAddress.getByAddress(bytes)
    }
}
