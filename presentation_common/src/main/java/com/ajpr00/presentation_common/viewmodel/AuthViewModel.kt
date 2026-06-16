package com.ajpr00.presentation_common.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.model.UserAuthData
import com.ajpr00.core.domain.usecase.login.FacebookAuthUseCase
import com.ajpr00.core.domain.usecase.login.LogoutUseCase
import com.ajpr00.core.domain.usecase.login.SaveSessionUseCase
import com.ajpr00.core.domain.usecase.user.ObserveCurrentUserUseCase
import com.ajpr00.core.domain.usecase.user.IsLoginStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * # AuthViewModel
 *
 * ViewModel responsable de gestionar **toda la lógica de autenticación** en la capa
 * *presentation*.
 *
 * Su misión es coordinar:
 * - Inicio de sesión con Facebook.
 * - Guardado de sesión local.
 * - Observación del estado de login.
 * - Cierre de sesión.
 * - Emisión de eventos hacia la UI.
 *
 * ## ¿Por qué existe este ViewModel?
 * La autenticación mezcla varias capas:
 *
 * - **domain** → casos de uso (login, logout, guardar sesión).
 * - **data** → repositorios (preferencias, API, etc.).
 * - **presentation** → UI que reacciona a estados y eventos.
 *
 * Este ViewModel actúa como puente limpio entre UI y domain, evitando que la UI
 * tenga que conocer detalles de infraestructura o repositorios.
 *
 * ## Estado expuesto
 * - `isLoggedIn`: indica si el usuario tiene sesión activa.
 * - `currentUser`: datos del usuario autenticado.
 * - `eventos`: mensajes de una sola emisión (snackbars, toasts, etc.).
 *
 * ## Advertencias
 * - No meter lógica de UI aquí.
 * - No llamar directamente a SDKs (Facebook, Google, etc.).
 * - No almacenar referencias a Activities.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val saveSessionUseCase: SaveSessionUseCase,
    private val facebookAuthUseCase: FacebookAuthUseCase,
    private val isLoginStateUseCase: IsLoginStateUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    /**
     * Flujo de eventos de una sola emisión.
     *
     * Se usa para mostrar mensajes en la UI (snackbar, toast, banners).
     * No se guarda estado, solo se emite.
     */
    private val _eventos = MutableSharedFlow<String>()
    val eventos = _eventos

    /**
     * Estado reactivo que indica si el usuario está logeado.
     *
     * Se obtiene desde domain mediante un `Flow<Boolean>` y se convierte
     * en `StateFlow` para que Compose pueda observarlo fácilmente.
     */
    val isLoggedIn = isLoginStateUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * Estado reactivo con los datos del usuario autenticado.
     *
     * Si no hay usuario, será `null`.
     */
    val currentUser =
        observeCurrentUserUseCase().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Guarda la sesión del usuario en almacenamiento local.
     *
     * ## Parámetros
     * - `data`: datos de autenticación del usuario.
     *
     * ## Notas
     * - No valida datos, solo delega al caso de uso.
     */
    fun saveSession(data: UserAuthData) {
        Log.d("AUTH_VM", "Guardando sesión del usuario…")
        viewModelScope.launch {
            saveSessionUseCase(data)
        }
    }

    /**
     * Procesa el token devuelto por Facebook Login.
     *
     * ## Flujo
     * 1. Llama al caso de uso `FacebookAuthUseCase`.
     * 2. Si es correcto → guarda sesión y emite evento de éxito.
     * 3. Si falla → emite evento de error.
     *
     * ## Advertencias
     * - No llamar desde la UI directamente.
     *   La UI debe delegar en `FacebookInitializer`.
     */
    fun onFacebookAccessToken(token: String) {
        Log.d("AUTH_VM", "Procesando token de Facebook…")

        viewModelScope.launch {
            val result = facebookAuthUseCase(token)

            result.onSuccess { user ->
                Log.d("AUTH_VM", "Login con Facebook exitoso. Guardando sesión…")

                saveSession(
                    UserAuthData(
                        token = user.token,
                        email = user.email,
                        username = user.username,
                        avatarUrl = user.avatarUrl
                    )
                )

                enviarEvento("Sesión iniciada correctamente")
            }

            result.onFailure { error ->
                Log.e("AUTH_VM", "Error en login de Facebook: ${error.message}")
                enviarEvento("Error al iniciar sesión con Facebook")
            }
        }
    }

    /**
     * Cierra la sesión del usuario.
     *
     * ## Flujo
     * 1. Llama al caso de uso `LogoutUseCase`.
     * 2. Si es correcto → emite evento de éxito.
     * 3. Si falla → emite evento de error.
     */
    fun logout() {
        Log.d("AUTH_VM", "Cerrando sesión…")

        viewModelScope.launch {
            val result = logoutUseCase()

            result.onSuccess {
                Log.d("AUTH_VM", "Sesión cerrada correctamente")
                enviarEvento("Sesión cerrada correctamente")
            }

            result.onFailure { error ->
                Log.e("AUTH_VM", "Error al cerrar sesión: ${error.message}")
                enviarEvento("Error al cerrar sesión")
                error.printStackTrace()
            }
        }
    }

    /**
     * Emite un evento de UI.
     *
     * ## Parámetros
     * - `mensaje`: texto que la UI mostrará al usuario.
     *
     * ## Notas
     * - No guarda estado, solo emite.
     */
    fun enviarEvento(mensaje: String) {
        Log.d("AUTH_VM", "Evento emitido: $mensaje")

        viewModelScope.launch {
            _eventos.emit(mensaje)
        }
    }
}