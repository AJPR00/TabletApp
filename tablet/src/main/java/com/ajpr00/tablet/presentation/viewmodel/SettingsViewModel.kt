package com.ajpr00.tablet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.model.qr.QrPayload
import com.ajpr00.core.domain.usecase.pairing.GetQrUseCase
import com.ajpr00.core.domain.usecase.preference.*
import com.ajpr00.tablet.presentation.state.SettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * # SettingsViewModel
 *
 * ViewModel responsable de gestionar **toda la configuración persistente** de la aplicación:
 *
 * - Apariencia (modo oscuro)
 * - Idioma
 * - Identidad de la tablet (ID + nombre)
 * - Estado de conexión móvil
 * - Nombre del móvil
 * - Clave AES (borrado)
 * - Regeneración del QR
 *
 * ## Arquitectura
 * - **Presentation**: expone `SettingsState`
 * - **Domain**: usa UseCases para leer/escribir preferencias
 * - **Data**: DataStore → AppPreference
 *
 * ## Notas
 * - El estado se construye combinando 6 Flows mediante `combine(listOf(...))`
 *   debido a que la versión actual de coroutines solo soporta combine hasta 5 parámetros.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getDarkMode: GetDarkModeUseCase,
    private val getLanguage: GetLanguageUseCase,
    private val getTabletId: GetTabletIdUseCase,
    private val getTabletName: GetTabletNameUseCase,
    private val getMobileConnected: GetMobileConnectedUseCase,
    private val getMobileName: GetMobileNameUseCase,
    private val setDarkMode: SetDarkModeUseCase,
    private val setLanguage: SetLanguageUseCase,
    private val setTabletName: SetTabletNameUseCase,
    private val setMobileConnected: SetMobileConnectedUseCase,
    private val setMobileName: SetMobileNameUseCase,
    private val saveAesKey: SaveAesKeyUseCase,
    private val getQrUseCase: GetQrUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    private val _qrPayload = MutableStateFlow<QrPayload?>(null)
    val qrPayload: StateFlow<QrPayload?> = _qrPayload


    init {
        observePreferences()
    }

    /**
     * ## observePreferences()
     *
     * Combina los 6 Flows de preferencias para construir el `SettingsState`.
     *
     * ### Por qué listOf()
     * La versión actual de coroutines **solo soporta combine hasta 5 parámetros**.
     * Para 6+ flows, se usa:
     *
     * ```kotlin
     * combine(listOf(flow1, flow2, ...)) { values -> ... }
     * ```
     */
    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                listOf(
                    getDarkMode(),
                    getLanguage(),
                    getTabletId(),
                    getTabletName(),
                    getMobileConnected(),
                    getMobileName()
                )
            ) { values ->
                SettingsState(
                    isDarkMode = values[0] as Boolean,
                    language = values[1] as String,
                    tabletId = values[2] as String,
                    tabletName = values[3] as String,
                    isMobileConnected = values[4] as Boolean,
                    mobileName = values[5] as String
                )
            }.collect { newState ->
                println("[SettingsVM] Nuevo estado recibido: $newState")
                _state.value = newState
            }
        }
    }

    // ---------------------------------------------------------
    // APARIENCIA
    // ---------------------------------------------------------

    /**
     * Actualiza el modo oscuro.
     */
    fun toggleDarkMode(enabled: Boolean) = viewModelScope.launch {
        println("[SettingsVM] Cambiando modo oscuro a: $enabled")
        setDarkMode(enabled)
    }

    /**
     * Cambia el idioma de la aplicación.
     */
    fun changeLanguage(lang: String) = viewModelScope.launch {
        println("[SettingsVM] Cambiando idioma a: $lang")
        setLanguage(lang)
    }

    // ---------------------------------------------------------
    // IDENTIDAD DE LA TABLET
    // ---------------------------------------------------------

    /**
     * Cambia el nombre de la tablet.
     */
    fun changeTabletName(name: String) = viewModelScope.launch {
        println("[SettingsVM] Cambiando nombre de la tablet a: $name")
        setTabletName(name)
    }

    // ---------------------------------------------------------
    // EMPAREJAMIENTO / QR
    // ---------------------------------------------------------

    /**
     * Regenera el QR llamando al servidor local.
     */
    fun regenerateQr() = viewModelScope.launch {
        println("[SettingsVM] Regenerando QR…")

        val result = getQrUseCase()

        result.onSuccess { payload ->
            println("[SettingsVM] QR recibido: $payload")
            _qrPayload.value = payload
        }

        result.onFailure { error ->
            println("[SettingsVM] ERROR generando QR: ${error.message}")
        }
    }


    /**
     * Borra la clave AES → desvincula el móvil.
     */
    fun clearAesKey() = viewModelScope.launch {
        println("[SettingsVM] Borrando clave AES y desvinculando móvil…")
        saveAesKey(ByteArray(0))
        setMobileConnected(false)
        setMobileName("Movil")
    }
}
