package com.ajpr00.visumloop.tablet.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.visumloop.tablet.data.datasource.local.preferences.LoginPreferences
import com.ajpr00.visumloop.tablet.data.repository.AuthRepository
import com.ajpr00.visumloop.tablet.presentation.state.AuthUiState
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val loginPreferences: LoginPreferences
) : ViewModel() {

    private var _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState


    var driveToken: String? = null
        private set

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
        driveToken = token
    }

    fun onGoogleAccountReceived(account: GoogleSignInAccount) {
        val idToken = account.idToken
        val authCode = account.serverAuthCode

        viewModelScope.launch {
            loginPreferences.saveTokens(idToken, authCode)
        }

        _uiState.value = _uiState.value.copy(
            idToken = idToken,
            authCode = authCode,
            email = account.email,
            nombreCompleto = listOfNotNull(account.givenName, account.familyName)
                .takeIf { it.isNotEmpty() }
                ?.joinToString(" ")
        )
    }


    fun loginWithGoogle(idToken: String, email: String, givenName: String?, familyName: String?) {
        Log.d("VIEWMODELLoginConGoogle", "INICIANDO... login con Google")
        viewModelScope.launch {
            //_uiState.update { it.copy(isLoading = true) }

            val result = repository.loginWithGoogle(idToken)

            if (result.isSuccess) { // Si el login con Google es exitoso
                Log.d("VIEWMODELLoginConGoogle", "EXISTOSO... login con Google")

                val nombreCompleto = listOfNotNull(givenName, familyName)
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(" ")
                    ?: "Usuario sin nombre"


                //  preferencesRepository.saveLogin(email, nombreCompleto)

                //  val sesionActiva = preferencesRepository.sesionActiva.first()
                //  val penaNombre = preferencesRepository.nombrePena.first()

            }
        }
    }
}
