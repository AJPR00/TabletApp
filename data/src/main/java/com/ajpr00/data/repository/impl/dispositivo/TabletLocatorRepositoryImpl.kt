package com.ajpr00.data.repository.impl.dispositivo

import android.util.Log
import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.model.EstadoDispositivo
import com.ajpr00.core.domain.model.mDNS.MdnsServiceInfo
import com.ajpr00.core.domain.repository.dispositivo.TabletLocatorRepository
import com.ajpr00.core.util.NetworkUtils.getBaseIp
import com.ajpr00.core.util.ordenarHostsPorCercania
import com.ajpr00.core.util.tryLocate
import com.ajpr00.data.datasource.network.MdnsResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.Semaphore
import javax.inject.Inject

/**
 * TabletLocatorRepositoryImpl
 * ---------------------------
 * Repositorio se encarga de "buscar tablets" en la red local.
 *
 * 1. Obtiene la IP base del móvil (ej: 192.168.1)
 * 2. Genera 254 IPs posibles (192.168.1.1 → 192.168.1.254)
 * 3. Lanza 254 corrutinas en paralelo (muy rápido)
 * 4. Cada corrutina llama a /ping en esa IP
 * 5. Si responde "OK", significa que es una tablet VisumLoop
 * 6. Emitimos un IP hacia el ViewModel
 */

class TabletLocatorRepositoryImpl @Inject constructor(
    private val resolver: MdnsResolver
) : TabletLocatorRepository {

    private val TAG = "TabletLocatorRepo"

    override suspend fun locateByLan(port: Int): Flow<String> = channelFlow {

        Log.d(TAG, "Iniciando búsqueda LAN en puerto $port")

        val baseIp = getBaseIp() ?: return@channelFlow

        Log.d(TAG, "IP base detectada: $baseIp.x")

        val semaphore = Semaphore(8)
        val hostsOrdenados = ordenarHostsPorCercania(baseIp)

        coroutineScope {
            hostsOrdenados.map { host ->
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val ip = "$baseIp.$host"
                        Log.d(TAG, "Probando IP: $ip")

                        val ipResult = tryLocate(ip, port)

                        ipResult?.let {
                            Log.d(TAG, "Tablet encontrada: $ipResult")
                            send(ipResult)
                        }
                    } finally {
                        semaphore.release()
                    }
                }
            }.awaitAll()
        }
        Log.d(TAG, "Escaneo LAN finalizado")
    }

    override suspend fun locateByManual(ip: String, port: Int): String? =
        withContext(Dispatchers.IO) {
            try {
                val ipResult = tryLocate(ip, port)

                ipResult?.let {
                    Log.d(TAG, "Tablet encontrada: $ipResult")
                    ipResult
                } ?: run {
                    Log.d(TAG, "Tablet no encontrada: $ip")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error conectando → ${e::class.java.name} - ${e.message}")
                null
            }
        }

    override fun discoverTablet(): Flow<MdnsServiceInfo> {
        return resolver.discover()
    }
}
