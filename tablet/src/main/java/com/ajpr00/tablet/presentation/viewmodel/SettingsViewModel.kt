package com.ajpr00.tablet.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.model.qr.QrPayload
import com.ajpr00.core.domain.usecase.pairing.GetQrUseCase
import com.ajpr00.core.domain.usecase.preference.session.GetLocalEmailUseCase
import com.ajpr00.core.domain.usecase.preference.setting.GetDarkModeUseCase
import com.ajpr00.core.domain.usecase.preference.setting.GetIdUseCase
import com.ajpr00.core.domain.usecase.preference.setting.GetLanguageUseCase
import com.ajpr00.core.domain.usecase.preference.setting.GetNameUseCase
import com.ajpr00.core.domain.usecase.preference.setting.GetnameMobileUseCase
import com.ajpr00.core.domain.usecase.preference.setting.SaveAesKeyUseCase
import com.ajpr00.core.domain.usecase.preference.setting.SetConnectedUseCase
import com.ajpr00.core.domain.usecase.preference.setting.SetDarkModeUseCase
import com.ajpr00.core.domain.usecase.preference.setting.SetLanguageUseCase
import com.ajpr00.core.domain.usecase.preference.setting.SetNameUseCase
import com.ajpr00.tablet.presentation.state.SettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    private val getTabletId: GetIdUseCase,
    private val getTabletName: GetNameUseCase,
    private val isConnectedRed: GetLocalEmailUseCase,
    private val getMobileName: GetnameMobileUseCase,
    private val setDarkMode: SetDarkModeUseCase,
    private val setLanguage: SetLanguageUseCase,
    private val setTabletName: SetNameUseCase,
    private val setMobileConnected: SetConnectedUseCase,
    private val setMobileName: SetNameUseCase,
    private val saveAesKey: SaveAesKeyUseCase,
    private val getQrUseCase: GetQrUseCase
) : ViewModel() {

    val TAG = "SettingsViewModel"
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    private val _qrPayload = MutableStateFlow<QrPayload?>(null)
    val qrPayload: StateFlow<QrPayload?> = _qrPayload

    private val _qrTimer = MutableStateFlow(0)
    val qrTimer: StateFlow<Int> = _qrTimer

    private val _showQrDialog = MutableStateFlow(false)
    val showQrDialog: StateFlow<Boolean> = _showQrDialog


    init {
        observePreferences()
    }

    fun startQrTimer(duration: Int = 60) {
        viewModelScope.launch {
            _qrTimer.value = duration
            _showQrDialog.value = true

            while (_qrTimer.value > 0) {
                delay(1000)
                _qrTimer.value -= 1
            }

            _showQrDialog.value = false
        }
    }

    fun closeQrDialog() {
        _showQrDialog.value = false
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
                    getMobileName()
                )
            ) { values ->
                SettingsState(
                    isDarkMode = values[0] as Boolean,
                    language = values[1] as String,
                    tabletId = values[2] as String,
                    tabletName = values[3] as String,
                    mobileName = values[4] as String
                )
            }.collect { newState ->
               Log.d(TAG, " Nuevo estado recibido: $newState")
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
    fun toggleDarkMode() = viewModelScope.launch {
       Log.d(TAG, " Cambiando modo oscuro a: ${!state.value.isDarkMode}")
        setDarkMode()
    }

    /**
     * Cambia el idioma de la aplicación.
     */
    fun changeLanguage(lang: String) = viewModelScope.launch {
       Log.d(TAG, " Cambiando idioma a: $lang")
        setLanguage(lang)
    }

    // ---------------------------------------------------------
    // IDENTIDAD DE LA TABLET
    // ---------------------------------------------------------

    /**
     * Cambia el nombre de la tablet.
     */
    fun changeTabletName(name: String) = viewModelScope.launch {
       Log.d(TAG, " Cambiando nombre de la tablet a: $name")
        setTabletName(name)
    }

    // ---------------------------------------------------------
    // EMPAREJAMIENTO / QR
    // ---------------------------------------------------------

    /**
     * Regenera el QR llamando al servidor local.
     */
    fun regenerateQr() = viewModelScope.launch {
        Log.d(TAG, "Regenerando QR…")

        val result = getQrUseCase()

        result.onSuccess { payload ->
            Log.d(TAG, "QR recibido: $payload")
            _qrPayload.value = payload
            startQrTimer()   // Iniciar temporizador aquí
        }

        result.onFailure { error ->
            Log.d(TAG, "ERROR generando QR: ${error.message}")
        }
    }

    /**
     * Borra la clave AES → desvincula el móvil.
     */
    fun clearAesKey() = viewModelScope.launch {
       Log.d(TAG, " Borrando clave AES y desvinculando móvil…")
        saveAesKey(ByteArray(0))
        setMobileConnected(false)
        setMobileName("Movil")
    }
}
