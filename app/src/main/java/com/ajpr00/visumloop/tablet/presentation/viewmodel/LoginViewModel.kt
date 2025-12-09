package com.ajpr00.visumloop.tablet.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.visumloop.tablet.data.datasource.local.preferences.LoginPreferences
import com.ajpr00.visumloop.tablet.data.repository.LoginRepository
import com.ajpr00.visumloop.tablet.presentation.state.LoginEvent
import com.ajpr00.visumloop.tablet.presentation.state.LoginState
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository,
    private val loginPreferences: LoginPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState

    private val _loginEvent = MutableStateFlow<LoginEvent>(LoginEvent.Idle)
    val loginEvent: StateFlow<LoginEvent> = _loginEvent

    init {
        viewModelScope.launch {
            // Inicializar con token si existe
            loginPreferences.idToken.collect { token ->
                _uiState.update { it.copy(driveToken = token) }
            }
            loginPreferences.idToken.collect { idToken ->
                _uiState.update { it.copy(idToken = idToken) }
            }
        }
    }

    // llamada desde la UI cuando obtienes GoogleSignInAccount
    fun onGoogleAccountReceived(account: GoogleSignInAccount?, appContext: Context) {
        if (account == null) {
            _loginEvent.value = LoginEvent.Error("Cuenta Google nula")
            return
        }

        viewModelScope.launch {
            _loginEvent.value = LoginEvent.Loading
            try {
                val acc = account.account ?: run {
                    _loginEvent.value = LoginEvent.Error("No se obtuvo 'Account' de Google")
                    return@launch
                }

                val token = withContext(Dispatchers.IO) {
                    GoogleAuthUtil.getToken(appContext, acc, "oauth2:${DriveScopes.DRIVE_READONLY}")
                }

                // guardado en preferencias
                loginPreferences.saveToken(token)

                _uiState.update { it.copy(driveToken = token, loading = false) }
                _loginEvent.value = LoginEvent.GoogleSuccess(token)
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false) }
                _loginEvent.value = LoginEvent.Error(e.localizedMessage ?: "Error al obtener token")
            }
        }
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

    fun clearEvents() {
        _loginEvent.value = LoginEvent.Idle
    }
}
