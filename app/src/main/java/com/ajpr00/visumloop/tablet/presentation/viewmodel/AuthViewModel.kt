package com.ajpr00.visumloop.tablet.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.visumloop.tablet.data.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    var idToken by mutableStateOf<String?>(null)
        private set

    var authCode by mutableStateOf<String?>(null)
        private set
    fun onGoogleAccountReceived(account: GoogleSignInAccount) {
        idToken = account.idToken
        authCode = account.serverAuthCode
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
