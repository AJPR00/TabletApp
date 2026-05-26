package com.ajpr00.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.ajpr00.mobile.presentation.state.RegisterDeviceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class RegisterDeviceViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterDeviceState())
    val uiState: StateFlow<RegisterDeviceState> = _uiState

    fun onToggleManualFields() {
        _uiState.update { it.copy(showManualFields = !it.showManualFields) }
    }

    fun onNombreChange(value: String) {
        _uiState.update { it.copy(nombre = value) }
        validate()
    }

    fun onIpChange(value: String) {
        _uiState.update { it.copy(ip = value) }
        validate()
    }
    fun showBuscarDialog(show: Boolean) {
        _uiState.update { it.copy(showBuscar = show) }
    }

    fun showQRDialog(show: Boolean) {
        _uiState.update { it.copy(showDialogQR = show) }
    }

    fun onBuscar() {}

    private fun validate() {
        _uiState.update { state ->
            state.copy(
                isValid = state.nombre.isNotBlank() && state.ip.isNotBlank()
            )
        }
    }

    fun reset() {
        _uiState.value = RegisterDeviceState()
    }
}
