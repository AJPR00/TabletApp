package com.ajpr00.presentation_common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.usecase.login.LogoutUseCase
import com.ajpr00.core.domain.usecase.user.ObserveCurrentUserUseCase
import com.ajpr00.core.domain.usecase.user.ObserveLoginStateUseCase
import com.ajpr00.presentation_common.state.Estado
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    observeLoginStateUseCase: ObserveLoginStateUseCase,
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _eventos = MutableSharedFlow<String>()
    val eventos = _eventos

    val isLoggedIn = observeLoginStateUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val currentUser =
        observeCurrentUserUseCase().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun logout() {
        viewModelScope.launch {
            val result = logoutUseCase()

            result.onSuccess {
                enviarEvento("Sesión cerrada correctamente")
            }

            result.onFailure {
                enviarEvento("Error al cerrar sesión")
                it.printStackTrace()
            }
        }
    }

    private fun enviarEvento(mensaje: String) {
        viewModelScope.launch {
            _eventos.emit(mensaje)
        }
    }

}

