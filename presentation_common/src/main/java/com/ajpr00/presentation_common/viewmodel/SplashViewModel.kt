package com.ajpr00.presentation_common.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.repository.preference.SettingsManager
import com.ajpr00.core.domain.usecase.user.IsLoginStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * # SplashViewModel
 *
 * ViewModel encargado de gestionar la lógica del **Splash Compose**.
 *
 * Su misión es preparar la app antes de entrar en el flujo principal:
 *
 * - Comprobar si el usuario está logeado.
 * - Cargar preferencias iniciales (tema, idioma).
 * - Ejecutar inicializaciones ligeras.
 * - Avisar a la UI cuando ya puede navegar.
 *
 * ## ¿Por qué existe este ViewModel?
 * El Splash no debe tener lógica pesada en la UI.
 * Aquí centralizamos toda la carga inicial para que:
 *
 * - El Splash Compose se muestre el tiempo justo.
 * - La navegación sea determinista (Main o Login).
 * - No haya pantallas en blanco ni saltos raros.
 *
 * ## Relación con otras capas
 * - **domain** → usa `IsLoginStateUseCase` para saber si hay sesión.
 * - **data** → usa `AppSettingsRepository` para cargar preferencias.
 * - **presentation** → expone estado reactivo para el Splash Compose.
 *
 * ## Estado expuesto
 * - `isReady`: indica si ya se puede navegar.
 * - `isLoggedIn`: indica si el usuario tiene sesión activa.
 *
 * ## Advertencias
 * - No meter lógica de UI aquí.
 * - No hacer llamadas de infraestructura pesadas (HTTP, CameraX, etc.).
 * - No bloquear el hilo principal.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val observeLoginStateUseCase: IsLoginStateUseCase,
    private val settingsRepository: SettingsManager
) : ViewModel() {

    /**
     * Estado interno que indica si el Splash ya puede desaparecer.
     *
     * - Empieza en `false` para que el Splash Compose se muestre.
     * - Se pone en `true` cuando toda la carga inicial ha terminado.
     */
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    /**
     * Estado reactivo que indica si el usuario está logeado.
     *
     * Se obtiene desde domain mediante un `Flow<Boolean>` y se convierte
     * en `StateFlow` para que Compose pueda observarlo sin problemas.
     */
    val isLoggedIn: StateFlow<Boolean> =
        observeLoginStateUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = false
            )

    init {
        Log.d("SPLASH", "Iniciando carga del SplashViewModel")

        viewModelScope.launch {

            // 1. Cargar estado de login
            isLoggedIn.first()
            Log.d("SPLASH", "Cargando estado de login… ${isLoggedIn.value}")

            // 2. Cargar preferencias del usuario
            Log.d("SPLASH", "Cargando preferencias (tema, idioma)…")
            settingsRepository.isDarkMode().first()
            settingsRepository.getLanguage().first()

            // 3. Inicializaciones ligeras
            Log.d("SPLASH", "Ejecutando inicializaciones ligeras…")
            preloadServices()

            // 4. Liberar Splash
            Log.d("SPLASH", "Splash listo → isReady = true")
            _isReady.value = true
        }
    }

    /**
     * Inicializaciones ligeras que se ejecutan durante el Splash.
     *
     * Aquí puedes precargar servicios, preparar caches o inicializar
     * componentes que no bloqueen el arranque.
     *
     * ## Notas
     * - No meter llamadas pesadas (HTTP, BD grande, etc.).
     * - No bloquear el hilo principal.
     */
    private suspend fun preloadServices() {
        Log.d("SPLASH", "preloadServices() ejecutado")
    }
}