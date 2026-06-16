package com.ajpr00.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.repository.preference.SettingsManager
import com.ajpr00.data.datasource.network.MdnsResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefsRepository: SettingsManager,
    private val mdnsResolver: MdnsResolver
) : ViewModel() {

    // ---------------------------------------------------------
    // ESTADOS EXPUESTOS A LA UI
    // ---------------------------------------------------------

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _tabletFound = MutableStateFlow(false)
    val tabletFound: StateFlow<Boolean> = _tabletFound

    private val _tabletIp = MutableStateFlow<String?>(null)
    val tabletIp: StateFlow<String?> = _tabletIp


    // ---------------------------------------------------------
    // PERMISOS
    // ---------------------------------------------------------

    fun setPermissionsGranted(value: Boolean) {
        _permissionsGranted.value = value
    }


    // ---------------------------------------------------------
    // BÚSQUEDA mDNS (solo móvil)
    // ---------------------------------------------------------

    fun startMdnsSearch() {
        if (!_permissionsGranted.value) return

        _isSearching.value = true

        viewModelScope.launch {

            mdnsResolver.startDiscovery(
                onDeviceFound = { device ->

                    // Guardamos la IP de la tablet
                    _tabletIp.value = device.ip

                    // Marcamos que la tablet fue encontrada
                    _tabletFound.value = true

                    // Paramos la búsqueda
                    _isSearching.value = false
                    mdnsResolver.stopDiscovery()
                }
            )

            // Timeout de seguridad (10s)
            delay(10_000)

            if (!_tabletFound.value) {
                _isSearching.value = false
                mdnsResolver.stopDiscovery()
            }
        }
    }


    // ---------------------------------------------------------
    // GUARDAR FLAG DE ONBOARDING COMPLETADO
    // ---------------------------------------------------------

    fun setOnboardingCompleted() {
        viewModelScope.launch {
            prefsRepository.setFirstRunCompleted()
        }
    }
}
