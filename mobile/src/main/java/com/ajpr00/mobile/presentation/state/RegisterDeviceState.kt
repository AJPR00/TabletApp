package com.ajpr00.mobile.presentation.state

import com.ajpr00.core.domain.model.Dispositivo
data class RegisterDeviceState(
    val showManualFields: Boolean = false,
    val showDialogQR: Boolean = false,
    val showListAutoLan: Boolean = false,
    val showListManualLan: Boolean = false,
    val nombre: String = "",
    val ip: String = "",
    val isValid: Boolean = false,
    val isSearchingAut: Boolean = false,
    val isPairingQr: Boolean = false,
    val isSearchingManual: Boolean = false,
    val foundDevices: List<Dispositivo> = emptyList()
)

