package com.ajpr00.visumloop.tablet.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.visumloop.tablet.data.datasource.local.preferences.LoginPreferences
import com.ajpr00.visumloop.tablet.data.repository.LoginRepository
import com.ajpr00.visumloop.tablet.presentation.state.LoginState
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.drive.DriveScopes
import com.google.firebase.auth.FirebaseAuth
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

    private val _mensajeEventot = MutableStateFlow<String>("")
    val mensaje: StateFlow<String> = _mensajeEventot

    fun setMensaje(ms: String) {
        _mensajeEventot.value = ms
    }

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
            _mensajeEventot.value = "No se obtuvo 'Account' de Google"
            return
        }

        viewModelScope.launch {
            try {
                val acc = account.account ?: run {
                    Log.d("LoginViewModel", "No se obtuvo 'Account' de Google")
                    return@launch
                }

                val token = withContext(Dispatchers.IO) {
                    GoogleAuthUtil.getToken(appContext, acc, "oauth2:${DriveScopes.DRIVE_READONLY}")
                }

                // guardado en preferencias
                loginPreferences.saveToken(token)

                _uiState.update { it.copy(driveToken = token, loading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false) }
                _mensajeEventot.value = "❌ Error al obtener verificacion"
                Log.e("LoginViewModel", "Error al obtener token", e)
            }
        }
    }

    fun registrarEmailPass(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "Error al registrar usuario")
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
}
