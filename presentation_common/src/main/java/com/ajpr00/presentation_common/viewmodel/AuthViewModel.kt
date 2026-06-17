package com.ajpr00.presentation_common.viewmodel

import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.model.UserAuthData
import com.ajpr00.core.domain.model.qr.Tablet
import com.ajpr00.core.domain.usecase.RegisterFirestoreTabletUseCase
import com.ajpr00.core.domain.usecase.login.FacebookAuthUseCase
import com.ajpr00.core.domain.usecase.login.LogoutUseCase
import com.ajpr00.core.domain.usecase.login.SaveSessionUseCase
import com.ajpr00.core.domain.usecase.preference.setting.GetIdUseCase
import com.ajpr00.core.domain.usecase.preference.setting.GetNameUseCase
import com.ajpr00.core.domain.usecase.preference.session.GetLocalEmailUseCase
import com.ajpr00.core.domain.usecase.user.ObserveCurrentUserUseCase
import com.ajpr00.core.domain.usecase.user.IsLoginStateUseCase
import com.ajpr00.core.util.NetworkUtils
import com.ajpr00.data.datasource.tablet.TabletFirestoreDataSource
import com.ajpr00.data.dto.dispositivo.TabletFirestoreDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * # AuthViewModel
 *
 * ViewModel encargado de gestionar **toda la lógica de autenticación y registro
 * de la tablet** dentro de la capa *presentation*.
 *
 * Su función es actuar como **puente limpio** entre la UI y los casos de uso de domain,
 * evitando que la UI tenga que conocer detalles de infraestructura (Firestore, DataStore,
 * Facebook SDK, etc.).
 *
 * ## Responsabilidades principales
 * - Gestionar el login con Facebook.
 * - Guardar la sesión local del usuario.
 * - Exponer el estado de autenticación en tiempo real.
 * - Cerrar sesión.
 * - Registrar la tablet en Firestore para sincronización remota.
 * - Emitir eventos de UI (snackbars, toasts, banners).
 *
 * ## Relación con otras capas
 * - **presentation** → expone estado y eventos.
 * - **domain** → ejecuta casos de uso (login, logout, obtener datos locales).
 * - **data** → delega en Firestore y DataStore.
 *
 * ## Advertencias
 * - No almacenar referencias a Activities.
 * - No llamar directamente a SDKs desde la UI.
 * - No bloquear el hilo principal.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val saveSessionUseCase: SaveSessionUseCase,
    private val facebookAuthUseCase: FacebookAuthUseCase,
    private val isLoginStateUseCase: IsLoginStateUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getNameUseCase: GetNameUseCase,
    private val getIdUseCase: GetIdUseCase,
    private val getEmailUseCase: GetLocalEmailUseCase,
    private val tabletFirestoreDataSource: RegisterFirestoreTabletUseCase
) : ViewModel() {

    // -------------------------------------------------------------------------
    // REGISTRO DE TABLET EN FIRESTORE
    // -------------------------------------------------------------------------

    /**
     * Registra la tablet en Firestore para que el backend pueda localizarla
     * y asociarla al usuario actual.
     *
     * ## ¿Por qué es necesario?
     * Firestore actúa como un **directorio global** donde cada tablet publica:
     * - Su ID único.
     * - Su IP local.
     * - Su modelo.
     * - Su estado (ONLINE/OFFLINE).
     * - Su usuario propietario.
     *
     * Esto permite que:
     * - El móvil pueda descubrir tablets incluso fuera de la LAN.
     * - Se pueda mostrar una lista de tablets asociadas al usuario.
     * - Se puedan enviar comandos remotos en futuras versiones.
     *
     * ## Flujo interno
     * 1. Obtiene datos locales desde DataStore:
     *    - `id`, `name`, `email`.
     * 2. Obtiene la IP local mediante `NetworkUtils`.
     * 3. Construye un `TabletFirestoreDto`.
     * 4. Llama al DataSource para guardar el documento en Firestore.
     *
     * ## Advertencias
     * - No valida conectividad; Firestore puede fallar silenciosamente.
     * - Si la IP es `null`, se guarda como cadena vacía.
     */
    fun registerTabletInFirestore() {
        viewModelScope.launch {
            Log.d("AUTH_VM", "===== REGISTRO DE TABLET EN FIRESTORE INICIADO =====")

            val id = getIdUseCase().first()
            val name = getNameUseCase().first()
            val email = getEmailUseCase().first()
            val ip = NetworkUtils.getLocalIpAddress()
            val port = 8080

            Log.d("AUTH_VM", "[1] Datos locales obtenidos:")
            Log.d("AUTH_VM", "    - id: $id")
            Log.d("AUTH_VM", "    - name: $name")
            Log.d("AUTH_VM", "    - email: $email")
            Log.d("AUTH_VM", "    - ip: ${ip ?: "null"}")
            Log.d("AUTH_VM", "    - port: $port")

            val tablet = Tablet(
                id = id,
                emailUsuario = email,
                modelo = Build.MODEL,
            )

            tabletFirestoreDataSource(tablet, ip ?: "", port, "ONLINE", System.currentTimeMillis())

            Log.d("AUTH_VM", "===== REGISTRO DE TABLET EN FIRESTORE COMPLETADO =====")
        }
    }

    // -------------------------------------------------------------------------
    // EVENTOS DE UI
    // -------------------------------------------------------------------------

    /**
     * Flujo de eventos de una sola emisión.
     *
     * Se usa para mostrar mensajes en la UI (snackbar, toast, banners).
     * No guarda estado; solo emite.
     */
    private val _eventos = MutableSharedFlow<String>()
    val eventos = _eventos

    // -------------------------------------------------------------------------
    // ESTADO DE AUTENTICACIÓN
    // -------------------------------------------------------------------------

    /**
     * Estado reactivo que indica si el usuario está logeado.
     *
     * Se obtiene desde domain mediante un `Flow<Boolean>` y se convierte
     * en `StateFlow` para que Compose pueda observarlo fácilmente.
     */
    val isLoggedIn = isLoginStateUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Estado reactivo con los datos del usuario autenticado.
     *
     * Si no hay usuario, será `null`.
     */
    val currentUser =
        observeCurrentUserUseCase().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // -------------------------------------------------------------------------
    // LOGIN / LOGOUT
    // -------------------------------------------------------------------------

    /**
     * Guarda la sesión del usuario en almacenamiento local.
     *
     * ## Parámetros
     * - `data`: datos de autenticación del usuario.
     *
     * ## Notas
     * - No valida datos; solo delega al caso de uso.
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

    // -------------------------------------------------------------------------
    // EVENTOS
    // -------------------------------------------------------------------------

    /**
     * Emite un evento de UI.
     *
     * ## Parámetros
     * - `mensaje`: texto que la UI mostrará al usuario.
     *
     * ## Notas
     * - No guarda estado; solo emite.
     */
    fun enviarEvento(mensaje: String) {
        Log.d("AUTH_VM", "Evento emitido: $mensaje")

        viewModelScope.launch {
            _eventos.emit(mensaje)
        }
    }
}