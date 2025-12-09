package com.ajpr00.visumloop.tablet.presentation.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.visumloop.tablet.data.datasource.local.preferences.LoginPreferences
import com.ajpr00.visumloop.tablet.data.repository.LoginRepository
import com.ajpr00.visumloop.tablet.presentation.state.LoginState
import com.ajpr00.visumloop.tablet.presentation.state.RegistroEstado
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository,
    private val loginPreferences: LoginPreferences
) : ViewModel() {

    private var _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState

    private val _stateRegistro = MutableStateFlow<RegistroEstado>(RegistroEstado.Inicial)
    val stadoRegistro: StateFlow<RegistroEstado> = _stateRegistro

    init {
        viewModelScope.launch {

            loginPreferences.idToken.collect { token ->
                _uiState.value = _uiState.value.copy(idToken = token)
            }
            loginPreferences.authCode.collect { code ->
                _uiState.value = _uiState.value.copy(authCode = code)
            }
        }
    }

    fun setDriveToken(token: String) {
        _uiState.value = _uiState.value.copy(driveToken = token)
    }


    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(showPassword = !_uiState.value.showPassword)
    }

    fun updatePassword(newPassword: String) {
        _uiState.value = _uiState.value.copy(password = newPassword)
    }

    fun updateEmail(newEmail: String) {
        _uiState.value = _uiState.value.copy(email = newEmail)
    }
}
