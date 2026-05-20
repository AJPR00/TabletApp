package com.ajpr00.presentation_common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.usecase.LogoutUseCase
import com.ajpr00.core.domain.usecase.ObserveCurrentUserUseCase
import com.ajpr00.core.domain.usecase.ObserveLoginStateUseCase
import com.ajpr00.presentation_common.state.EstadoEvento
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _eventState = MutableStateFlow<EstadoEvento>(EstadoEvento.Inicial)
    val eventState: StateFlow<EstadoEvento> = _eventState

    val isLoggedIn = observeLoginStateUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val currentUser = observeCurrentUserUseCase().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun logout() {
        viewModelScope.launch {
            val result = logoutUseCase()

            result.onSuccess {
                addEvento("Sesion cerrada correctamente")
            }

            result.onFailure { error ->
                addEvento("Error al cerrar sesion")
                error.printStackTrace()
            }
        }
    }

    private fun addEvento(error: String) {
        val current = _eventState.value
        val nuevaLista = when (current) {
            is EstadoEvento.Mensajes -> current.mensajes.toMutableList().apply {add(error)}
            else -> mutableListOf(error)
        }
        _eventState.value = EstadoEvento.Mensajes(nuevaLista)
    }

    fun clearErrors() {
        _eventState.value = EstadoEvento.Inicial
    }
}

