package com.ajpr00.mobile.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.model.Dispositivo
import com.ajpr00.core.domain.repository.dispositivo.TabletLocatorRepository
import com.ajpr00.core.domain.unum.ConnectionMode
import com.ajpr00.core.domain.usecase.user.IsLoginStateUseCase
import com.ajpr00.core.domain.usecase.network.CheckInternetConnectionUseCase
import com.ajpr00.data.exception.DatabaseException
import com.ajpr00.data.exception.NetworkException
import com.ajpr00.data.repository.tablet.DiscovermDNSTabletUseCase
import com.ajpr00.data.repository.tablet.GetDispositivosUseCase
import com.ajpr00.data.repository.tablet.UpdateDispositivoUseCase
import com.ajpr00.presentation_common.connectivity.NetworkObserver
import com.ajpr00.presentation_common.connectivity.NetworkObserverManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar el **modo de conexión** del dispositivo móvil.
 *
 * Este ViewModel pertenece a la capa **presentation (mobile)** y su función es
 * coordinar tres fuentes de información:
 *
 * 1. Estado real de Internet (WAN) mediante.
 * 2. Descubrimiento LAN mediante mDNS con.
 * 3. Lista de tablets registradas en la BD local mediante.
 *
 * A partir de estos datos, determina el **ConnectionMode**:
 *
 * - `LAN` → estás en tu red local y tus tablets están accesibles.
 * - `WAN` → tienes Internet pero no estás en tu LAN.
 * - `OFFLINE` → no hay Internet ni tablets detectadas.
 *
 * ## Responsabilidades principales
 * - Monitorizar conectividad real (WAN).
 * - Detectar tablets en LAN mediante mDNS.
 * - Actualizar IPs en la BD local cuando cambian.
 * - Decidir si usar API local o Firebase.
 * - Exponer estado reactivo a la UI.
 *
 * ## Qué NO hace este ViewModel
 * - No realiza llamadas HTTP.
 * - No gestiona Firebase directamente.
 * - No contiene lógica de UI.
 * - No contiene lógica de almacenamiento.
 *
 * ## Flujo interno resumido
 * ```
 * startNetworkMonitoring() → cambios WAN
 * startMdnsDiscovery() → detección LAN
 * evaluateConnectionMode() → decide LAN/WAN/OFFLINE
 * resolveAccessAndUseService() → decide API local o Firebase
 * ```
 *
 * ## Advertencias importantes
 * - mDNS puede tardar varios segundos en detectar dispositivos.
 * - La detección LAN tiene TTL para evitar falsos positivos.
 * - La BD local puede no estar sincronizada con la red real.
 *
 * ## Excepciones
 * - [NetworkException] si falla la comprobación de Internet.
 * - [DatabaseException] si falla la actualización de IPs.
 * - [VisumException] si ocurre un error inesperado en el flujo.
 */
@HiltViewModel
class MobileConnectionViewModel @Inject constructor(
    private val isLoginStateUseCase: IsLoginStateUseCase,
    private val checkInternetConnectionUseCase: CheckInternetConnectionUseCase,
    private val discovermDNSTabletUseCase: DiscovermDNSTabletUseCase,
    private val getDispositivosUseCase: GetDispositivosUseCase,
    private val updateDispositivoUseCase: UpdateDispositivoUseCase,
    private val tabletLocatorRepository: TabletLocatorRepository,
    private val networkObserverManager: NetworkObserverManager
) : ViewModel() {

    private val TAG = "MobileConnectionVM"

    private val _connectionMode = MutableStateFlow(ConnectionMode.OFFLINE)
    val connectionMode: StateFlow<ConnectionMode> = _connectionMode

    private val _hasInternet = MutableStateFlow(false)
    val hasInternet: StateFlow<Boolean> = _hasInternet

    private var observer: NetworkObserver? = null

    private var lastDetectedInLan: Boolean = false
    private var lastDetectedIds: Set<String> = emptySet()
    private var lastLanDetectionTs: Long = 0L
    private val lanDetectionTtlMs: Long = 30_000L

    private val _dispositivos = MutableStateFlow<List<Dispositivo>>(emptyList())

    init {
        viewModelScope.launch {
            Log.d(TAG, "Observando dispositivos de la BD local")
            getDispositivosUseCase().collectLatest {
                _dispositivos.value = it
            }
        }

        // Descubrimiento inicial
        startMdnsDiscovery(5_000L)
    }

    /**
     * Actualiza el estado WAN (Internet real).
     *
     * @param online `true` si hay Internet real, `false` si no.
     */
    fun updateInternetState(online: Boolean) {
        Log.d(TAG, "updateInternetState: $online")
        _hasInternet.value = online

        if (online) {
            Log.d(TAG, "Internet recuperado → relanzando discovery corto")
            startMdnsDiscovery(5_000L)
        }

        evaluateConnectionMode()
    }

    /**
     * Evalúa el modo de conexión actual combinando:
     * - Última detección LAN válida.
     * - Estado WAN actual.
     */
    private fun evaluateConnectionMode() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val lanValid = lastDetectedInLan && (now - lastLanDetectionTs <= lanDetectionTtlMs)

            _connectionMode.value = when {
                lanValid -> ConnectionMode.LAN
                _hasInternet.value -> ConnectionMode.WAN
                else -> ConnectionMode.OFFLINE
            }

            Log.d(TAG, "ConnectionMode -> ${_connectionMode.value} (lanValid=$lanValid)")
        }
    }

    /**
     * Notifica al ViewModel que la app ha arrancado.
     * Se usa para lanzar un discovery inicial.
     */
    fun onAppStarted() {
        viewModelScope.launch {
            Log.d(TAG, "onAppStarted → lanzando discovery inicial")
            startMdnsDiscovery()
        }
    }

    /**
     * Inicia el proceso de descubrimiento mDNS.
     *
     * @param withTimeout Tiempo máximo de escucha.
     *
     * @throws NetworkException si falla el flujo mDNS.
     * @throws DatabaseException si falla la actualización de IPs.
     */
    fun startMdnsDiscovery(withTimeout: Long = 10_000L) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Iniciando mDNS discovery (timeout=$withTimeout ms)")

                val detectedIds = mutableSetOf<String>()
                lastDetectedIds = emptySet()
                val tabletsBDSnapshot = _dispositivos.value

                withTimeoutOrNull(withTimeout) {
                    discovermDNSTabletUseCase().collect { info ->
                        val detectedId = info.txt
                        val detectedIp = info.ip

                        if (!detectedIds.add(detectedId)) return@collect

                        val tablet = tabletsBDSnapshot.firstOrNull { it.id == detectedId }
                        if (tablet != null && tablet.ip != detectedIp) {
                            try {
                                updateDispositivoUseCase(tablet.copy(ip = detectedIp))
                                Log.d(TAG, "IP actualizada: ${tablet.id} -> $detectedIp")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error actualizando IP de ${tablet.id}", e)
                            }
                        }
                    }
                }

                lastDetectedIds = detectedIds.toSet()
                lastDetectedInLan = lastDetectedIds.isNotEmpty()
                lastLanDetectionTs = if (lastDetectedInLan) System.currentTimeMillis() else 0L

                Log.d(TAG, "mDNS finalizado. detectadas=${detectedIds.size}")

                evaluateConnectionMode()
                resolveAccessAndUseService()

            } catch (e: Exception) {
                Log.e(TAG, "Error en startMdnsDiscovery", e)
            }
        }
    }

    /**
     * Decide qué servicio usar según el contexto:
     *
     * - HOME LAN → API local
     * - OTHER LAN → Firebase
     * - WAN → Firebase
     * - OFFLINE → modo offline
     */
    fun resolveAccessAndUseService() {
        viewModelScope.launch {
            val homeIds = _dispositivos.value.map { it.id }.toSet()

            val inMyHomeLan = lastDetectedIds.intersect(homeIds).isNotEmpty()
            val inLan = lastDetectedInLan
            val hasInternet = _hasInternet.value

            when {
                inMyHomeLan -> {
                    Log.d(TAG, "Contexto: HOME LAN → usar API local")
                    useLocalApi()
                }
                inLan && !inMyHomeLan -> {
                    Log.d(TAG, "Contexto: OTHER LAN → usar Firebase")
                    useFirebase()
                }
                !inLan && hasInternet -> {
                    Log.d(TAG, "Contexto: WAN → usar Firebase")
                    useFirebase()
                }
                else -> {
                    Log.d(TAG, "Contexto: OFFLINE")
                    handleOffline()
                }
            }
        }
    }

    /**
     * Inicia la monitorización WAN mediante NetworkObserver.
     */
    fun startNetworkMonitoring() {
        Log.d(TAG, "Iniciando monitorización WAN")
        observer = networkObserverManager.start { isOnline ->
            updateInternetState(isOnline)
        }
    }

    private fun useLocalApi() { /* implementar llamadas locales */ }
    private fun useFirebase() { /* implementar llamadas a Firebase */ }
    private fun handleOffline() { /* comportamiento offline */ }
}
