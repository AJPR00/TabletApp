package com.ajpr00.visumloop.tablet.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.visumloop.tablet.data.repository.AppleAuthRepository
import com.ajpr00.visumloop.tablet.data.repository.DropboxAuthRepository
import com.ajpr00.visumloop.tablet.data.repository.EmailAuthRepository
import com.ajpr00.visumloop.tablet.data.repository.FtpAuthRepository
import com.ajpr00.visumloop.tablet.data.repository.GoogleAuthRepository
import com.ajpr00.visumloop.tablet.presentation.state.EstadoEvento
import com.ajpr00.visumloop.tablet.presentation.state.LoginState
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val googleRepo: GoogleAuthRepository,
    private val dropboxRepo: DropboxAuthRepository,
    private val appleRepo: AppleAuthRepository,
    private val emailRepo: EmailAuthRepository,
    private val ftpRepo: FtpAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState

    private val _eventState = MutableStateFlow<EstadoEvento>(EstadoEvento.Inicial)
    val eventState: StateFlow<EstadoEvento> = _eventState

    val isLocalLogged: StateFlow<Boolean> = googleRepo.isLocalLoggedIn
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        viewModelScope.launch {

        }
    }

    // llamada desde la UI cuando obtienes GoogleSignInAccount
    /*
        fun onGoogleAccountReceived(account: GoogleSignInAccount?, appContext: Context) {
            if (account == null) {
                Log.d("LoginViewModel", "onGoogleAccountReceived -> No se obtuvo 'Account' de Google")
                return
            }
            _uiState.update { it.copy(loading = true) }
            Log.d("LoginViewModel", "onGoogleAccountReceived -> Account recibido: $account")

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
                   addEvento("❌ Error al obtener verificacion")
                    Log.e("LoginViewModel", "Error al obtener token", e)
                } finally {
                    // Opcional: limpiar a Inicial tras unos segundos para resetear la UI
                    viewModelScope.launch {
                        delay(2000)
                        _uiState.update { it.copy(loading = false) }
                    }
                }
            }
        }
    */
    fun loginGoogleFirebase(
        account: GoogleSignInAccount?,
    ) {
        if (account == null) {
            Log.d("LoginViewModel", "No se obtuvo 'Account' de Google")
            return
        }
        viewModelScope.launch {
            try {
                val idToken = account.idToken
                val email = account.email

                if (idToken.isNullOrBlank() || email.isNullOrBlank()) {
                    Log.d("LoginViewModel", "Token o email inválido")
                    return@launch
                }

                val result = googleRepo.loginWithGoogleFirebase(idToken) //Registro en firebase
                if (result.isSuccess) {
                    // Guardar sesión en DataStore
                    googleRepo.saveGoogleLocal(email, idToken)

                } else {
                    addEvento("Error al iniciar sesión con Google")
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error al iniciar sesión con Google", e)
            }
        }
    }

    fun loginGoogleDrive(
        account: GoogleSignInAccount?,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (account == null) {
            onError("No se obtuvo cuenta de Google Drive")
            return
        }

        viewModelScope.launch {
            try {
                val acc = account.account ?: run {
                    onError("No se obtuvo 'Account' de Google")
                    return@launch
                }

                // Obtener access_token solo para Drive
                val token = withContext(Dispatchers.IO) {
                    GoogleAuthUtil.getToken(context, acc, "oauth2:${DriveScopes.DRIVE_READONLY}")
                }

                // Guardar en DataStore (sin tocar FirebaseAuth)
                googleRepo.saveDriveSession(email = account.email ?: "", idToken = token)

                _uiState.update { it.copy(driveToken = token, loading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false) }
                onError("❌ Error al obtener token de Drive: ${e.message}")
                Log.e("LoginViewModel", "Error al obtener token Drive", e)
            }
        }
    }

    fun loginEmailFirebase(
        email: String?,
        password: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            onError("Se requiere Email y Contraseña")
            return
        }

        viewModelScope.launch {
            val result = emailRepo.loginEmail(email, password)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError("Email o contraseña incorrectos")
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
