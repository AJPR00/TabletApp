package com.ajpr00.mobile.presentation.state

data class RegisterDeviceState(
val showManualFields: Boolean = false,
val showDialogQR: Boolean = false,
val showBuscar: Boolean = false,
val nombre: String = "",
val ip: String = "",
val isValid: Boolean = false
)
