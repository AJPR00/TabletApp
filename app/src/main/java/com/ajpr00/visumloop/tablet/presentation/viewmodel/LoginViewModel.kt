package com.ajpr00.visumloop.tablet.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.visumloop.tablet.BuildConfig
import com.ajpr00.visumloop.tablet.domain.model.UserGoogle
import com.ajpr00.visumloop.tablet.domain.usecase.LoginWithDropboxUseCase
import com.ajpr00.visumloop.tablet.domain.usecase.LoginWithEmailUseCase
import com.ajpr00.visumloop.tablet.domain.usecase.LoginWithFTPUseCase
import com.ajpr00.visumloop.tablet.domain.usecase.LoginWithFacebookUseCase
import com.ajpr00.visumloop.tablet.domain.usecase.LoginWithGoogleUseCase
import com.ajpr00.visumloop.tablet.domain.usecase.LogoutUseCase
import com.ajpr00.visumloop.tablet.presentation.state.EstadoEvento
import com.ajpr00.visumloop.tablet.presentation.state.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _eventState = MutableStateFlow<EstadoEvento>(EstadoEvento.Inicial)
    val eventState: StateFlow<EstadoEvento> = _eventState

    fun onGoogleLoginClick(context: Context) {
        viewModelScope.launch {
            val result = googleAuthUseCase(context)
            if (result.isSuccess) {
                addEvento("✅ Login correcto")
            } else {
                addEvento("❌ Login incorrecto")
            }
        }
    }

    fun addEvento(error: String) {
        val current = _eventState.value
        val nuevaLista = when (current) {
            is EstadoEvento.Mensajes -> current.mensajes.toMutableList().apply { add(error) }
            else -> mutableListOf(error)
        }
        _eventState.value = EstadoEvento.Mensajes(nuevaLista)
    }

    fun clearErrors() {
        _eventState.value = EstadoEvento.Inicial
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
}
