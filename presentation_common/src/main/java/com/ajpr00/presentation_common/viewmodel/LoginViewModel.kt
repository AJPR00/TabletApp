package com.ajpr00.presentation_common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.usecase.login.LoginWithDropboxUseCase
import com.ajpr00.core.domain.usecase.login.LoginWithEmailUseCase
import com.ajpr00.core.domain.usecase.login.LoginWithFTPUseCase
import com.ajpr00.core.domain.usecase.login.LoginWithFacebookUseCase
import com.ajpr00.core.domain.usecase.login.LoginWithGoogleUseCase
import com.ajpr00.core.domain.usecase.login.LogoutUseCase
import com.ajpr00.presentation_common.state.Estado
import com.ajpr00.presentation_common.state.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val googleAuthUseCase: LoginWithGoogleUseCase,
    private val facebookAuthUseCase: LoginWithFacebookUseCase,
    private val dropboxAuthUseCase: LoginWithDropboxUseCase,
    private val emailAuthUseCase: LoginWithEmailUseCase,
    private val ftpAuthUseCase: LoginWithFTPUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState

    private val _state = MutableStateFlow<Estado>(Estado.Inicial)
    val State: StateFlow<Estado> = _state

    private val _eventos = MutableSharedFlow<String>()
    val eventos = _eventos

    fun onGoogleLoginClick() {
        viewModelScope.launch {
            val result = googleAuthUseCase()
            if (result.isSuccess) {
                addEvento("Login correcto")
            } else {
                addEvento("Login incorrecto")
            }
        }
    }


    fun clearErrors() {
        _state.value = Estado.Inicial
    }

    fun updateEmail(newEmail: String) {
        _uiState.update { it.copy(email = newEmail) }
    }

    fun updatePassword(newPassword: String) {
        _uiState.update { it.copy(password = newPassword) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(showPassword = !it.showPassword) }
    }

    fun addEvento(mensaje: String) {
        viewModelScope.launch {
            _eventos.emit(mensaje)
        }
    }
}
