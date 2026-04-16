/*
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
import com.ajpr00.visumloop.tablet.domain.usecase.LoginWithAppleUseCase
import com.ajpr00.visumloop.tablet.domain.usecase.LoginWithDropboxUseCase
import com.ajpr00.visumloop.tablet.domain.usecase.LoginWithEmailUseCase
import com.ajpr00.visumloop.tablet.domain.usecase.LoginWithFTPUseCase
import com.ajpr00.visumloop.tablet.domain.usecase.LoginWithFacebookUseCase
import com.ajpr00.visumloop.tablet.domain.usecase.LoginWithGoogleUseCase
import com.ajpr00.visumloop.tablet.domain.usecase.LogoutUseCase
import com.ajpr00.visumloop.tablet.presentation.state.EstadoEvento
import com.ajpr00.visumloop.tablet.presentation.state.LoginState
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModelCopy @Inject constructor(
    private val googleAuthUseCase: LoginWithGoogleUseCase,
    private val facebookAuthUseCase: LoginWithFacebookUseCase,
    private val dropboxAuthUseCase: LoginWithDropboxUseCase,
    private val appleAuthUseCase: LoginWithAppleUseCase,
    private val emailAuthUseCase: LoginWithEmailUseCase,
    private val ftpAuthUseCase: LoginWithFTPUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState

    private val _eventState = MutableStateFlow<EstadoEvento>(EstadoEvento.Inicial)
    val eventState: StateFlow<EstadoEvento> = _eventState


    val isLocalLogged: StateFlow<Boolean> = googleAuthRepo.isLocalLoggedIn
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    */
/**
     * Login con contraseña guardada en el sistema.
     * Si no hay ninguna, lanzará NoCredentialException.
     *//*

    private suspend fun getPasswordCredential(context: Context): PasswordCredential {
        val option = GetPasswordOption()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val manager = CredentialManager.create(context)
        val response = manager.getCredential(context, request)

        return response.credential as PasswordCredential
    }

    */
/**
     * Login con Passkey (huella / FaceID / PIN) vía WebAuthn/FIDO2.
     * Requiere backend propio para validación.
     *//*

    private suspend fun getPasskeyCredential(context: Context, requestJson: String): PublicKeyCredential {
        val option = GetPublicKeyCredentialOption(requestJson)

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val manager = CredentialManager.create(context)
        val response = manager.getCredential(context, request)

        return response.credential as PublicKeyCredential
    }

    // ---------------------------------------------------------
    // 2) PROCESAMIENTO DE CREDENCIALES (único punto de entrada)
    // ---------------------------------------------------------

    */
/**
     * Aquí llega cualquier tipo de credencial que el sistema nos entregue.
     *
     * Según lo que haya elegido el usuario, Android nos da:
     *
     * - Contraseña → el sistema busca en su gestor de contraseñas y nos pasa usuario/clave.
     * - Passkey → se activa la autenticación biométrica (huella, cara, PIN).
     * - Custom → credenciales de proveedores externos (Google, etc.) según tu propio flujo.
     *
     * Esta función solo decide quien va a manejar la credecial obtenida.
     * Nada de lógica rara aquí, solo enrutar.
     *//*


   private suspend fun handleCredential(credential: Credential) {
        when (credential) {
            is CustomCredential -> handleCustomCredential(credential)
            is PasswordCredential -> handlePasswordCredential(credential)
            is PublicKeyCredential -> handlePasskeyCredential(credential)
            else -> throw IllegalArgumentException("Tipo de credencial desconocido")
        }
    }

    // ---------------------------------------------------------
    // 3) MANEJO DE CREDENCIALES SEGÚN TIPO
    // ---------------------------------------------------------

    */
/**
     * Manejo de CustomCredential:
     * Se define como se gestiona los dats obtenidos de diferentes proveedores.
     *
     * - GoogleIdTokenCredential (ID Token JWT)
     * - Facebook (Access Token)
     * - Otros proveedores
     *//*

    private suspend fun handleCustomCredential(credential: CustomCredential) {
            when (credential.type) {

                // -------------------------
                // GOOGLE
                // -------------------------
                GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                    Log.d("Login", "Obtenida CustomCredential de Google")

                    val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleCred.idToken

                    val result = googleAuthRepo.loginFirebase(idToken)

                    if (result.isSuccess) {
                        googleAuthRepo.saveGoogleLocal(
                            UserGoogle(
                                idToken = googleCred.idToken,
                                email = googleCred.id,
                                name = googleCred.displayName,
                                avatarUrl = googleCred.profilePictureUri?.toString()
                            )
                        )
                        addEvento("✅ Login correcto")
                        Log.d("Login", "Login correcto (Google)")
                    } else {
                        addEvento("❌ Login incorrecto")
                        Log.d("Login", "Login incorrecto (Google)")
                    }
                }

                // -------------------------
                // FACEBOOK (ejemplo)
                // -------------------------
                */
/*
                "com.facebook.auth.ACCESS_TOKEN" -> {
                    val fbAccessToken = String(credential.data)
                    val result = facebookAuthRepo.loginFirebase(fbAccessToken)

                    if (result.isSuccess) {
                        addEvento("✅ Login correcto (Facebook)")
                    } else {
                        addEvento("❌ Login incorrecto (Facebook)")
                    }
                }
                *//*


                else -> {
                    Log.w("Login", "CustomCredential desconocida: ${credential.type}")
                }
            }
    }

    */
/**
     * Manejo de Passkeys (WebAuthn/FIDO2).
     * Requiere backend propio para validar la firma.
     *//*

    private fun handlePasskeyCredential(credential: PublicKeyCredential) {
        val json = credential.authenticationResponseJson
        Log.d("Login", "Obtenida PublicKeyCredential con JSON: $json")
        // Aquí enviarías el JSON a tu backend para validación WebAuthn
    }

    private fun handlePasswordCredential(credential: PasswordCredential) {
        viewModelScope.launch {
            val username =
                credential.id       // normalmente el email o nombre de usuario
            val password = credential.password // la contraseña guardada

            val result = emailAuthRepo.loginEmail(username, password)
            if (result.isSuccess) {
                addEvento("✅ Login correcto")
                Log.d("Login", "Usuario/contraseña correcto")
            } else {
                addEvento("❌ Login incorrecto")
                Log.d("Login", "Usuario/contraseña incorrecto")
            }
            Log.d("Login", "Obtenida PasswordCredential, manejar usuario/contraseña")
        }
    }

    // ---------------------------------------------------------
    // 4) ACCIONES DE UI
    // ---------------------------------------------------------

    fun onGoogleLoginClick(context: Context) {
        viewModelScope.launch {
            try {
                val credential = getGoogleSignInCredential(context)
                handleCredential(credential)
            } catch (e: Exception) {
                addEvento("❌ Error en login con Google")
            }
        }
    }

    fun onPasswordLoginClick(context: Context) {
        viewModelScope.launch {
            try {
                val credential = getPasswordCredential(context)
                handleCredential(credential)
            } catch (e: Exception) {
                addEvento("❌ No hay contraseña guardada")
            }
        }
    }

    */
/*fun tryAutoLogin(context: Context) {
        viewModelScope.launch {
            try {
                val credential = getGoogleIdCredential(context)
                handleCredential(credential)
            } catch (_: Exception) {
                // No hay autologin → mostrar pantalla de login
            }
        }
    }*//*


    */
/*  fun loginGoogleDrive(
          jwtToken: String?,
          context: Context,
          onSuccess: () -> Unit,
          onError: (String) -> Unit
      ) {
          if (jwtToken.isNullOrBlank()) {
              onError("No se obtuvo cuenta de Google Drive")
              return
          }

          viewModelScope.launch {
              try {
                  // 1️⃣ Crear un GoogleIdTokenAccount a partir del JWT
                  val account = GoogleIdTokenAccount.from(jwtToken)

                  // 2️⃣ Construir la petición de autorización con los scopes que necesitas
                  val request = AuthorizationRequest(
                      googleIdTokenAccount = account,
                      scopes = listOf(
                          DriveScopes.DRIVE_READONLY // Cambia al scope que necesites
                      )
                  )

                  // 3️⃣ Autorizar usando el AuthorizationClient moderno
                  val result = AuthorizationClient.authorize(context, request)

                  // 4️⃣ Obtener el Access Token OAuth2
                  val accessToken = result.accessToken
                  if (accessToken.isNullOrBlank()) {
                      onError("No se pudo obtener Access Token de Drive")
                      return@launch
                  }

                  // 5️⃣ Guardar sesión de Drive en tu repo local
                  googleAuthRepo.saveDriveSession(
                      email = account.email ?: "",
                      accessToken = accessToken
                  )

                  // 6️⃣ Actualizar UI state y notificar éxito
                  _uiState.update { it.copy(driveToken = accessToken, loading = false) }
                  onSuccess()

              } catch (e: Exception) {
                  // Manejo de errores
                  _uiState.update { it.copy(loading = false) }
                  onError("❌ Error al obtener token de Drive: ${e.message}")
                  Log.e("LoginViewModel", "Error al obtener token Drive", e)
              }
          }
      }*//*


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
            val result = emailAuthRepo.loginEmail(email, password)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError("Email o contraseña incorrectos")
            }
        }
    }

    // ---------------------------------------------------------
    // 5) UTILIDADES
    // ---------------------------------------------------------

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
*/
