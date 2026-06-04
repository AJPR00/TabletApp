package com.ajpr00.presentation_common.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajpr00.core.domain.usecase.user.CheckUserExistsUseCase
import com.ajpr00.core.domain.usecase.user.RegisterUserUseCase
import com.ajpr00.core.domain.usecase.login.SendPasswordResetUseCase
import com.ajpr00.core.util.validarEmail
import com.ajpr00.core.util.validarPassword
import com.ajpr00.presentation_common.state.Estado
import com.ajpr00.presentation_common.state.RegisterState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase,
    private val checkUserExistsUseCase: CheckUserExistsUseCase,
    private val sendPasswordResetUseCase: SendPasswordResetUseCase
) : ViewModel() {

    // Estado principal del registro: aquí guardamos email, password, confirmaciones, etc.
    private val _uiState = MutableStateFlow(RegisterState())
    val uiState: StateFlow<RegisterState> = _uiState

    private val _state = MutableStateFlow<Estado>(Estado.Inicial)
    val state: StateFlow<Estado> = _state

    private val _eventos = MutableSharedFlow<String>()
    val eventos = _eventos


    /**
     * Función que intenta registrar un usuario con Firebase usando email y password.
     * Si todo va bien, emitimos un mensaje de éxito. Si falla, emitimos un mensaje de error.
     */
    fun registrar() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (!isEmailValid() || !isPasswordValid()) return

        viewModelScope.launch {
            _state.value = Estado.Cargando
            Log.d("RegisterViewModel", "Intentando registrar usuario con email: $email")
            val exists = checkUserExistsUseCase(email)

            if (exists) {
                Log.d("RegisterViewModel", "El usuario ya existe")
                enviarEvento("El usuario ya existe")
                return@launch
            }

            val result = registerUserUseCase(email, password)

            if (result.isSuccess) {
                Log.d("RegisterViewModel", "Registro exitoso en Firebase")
                enviarEvento("Cuenta creada correctamente")
                _state.value = Estado.Exito

            } else {
                Log.e("RegisterViewModel", "${result.exceptionOrNull()?.message}")
                showError("Error al registrar usuario")
            }
        }

    }

    fun recuperarPassword(email: String) {
        viewModelScope.launch {
            _state.value = Estado.Cargando

            val result = sendPasswordResetUseCase(email)

            if (result.isSuccess) {
                enviarEvento("Correo de recuperación enviado a $email")
                _state.value =  Estado.Exito
            } else {
                showError("Error al enviar correo de recuperación")
            }
            delay(5000)
            _state.value = Estado.Inicial
        }
    }


    fun clearErrors() {
        _state.value = Estado.Inicial
    }

    fun clearFormularios() {
        _uiState.update {
            it.copy(
                email = "",
                confirmEmail = "",
                password = "",
                confirmPassword = ""
            )
        }
    }

    // Actualizamos el email principal
    fun updateEmail(newEmail: String) {
        Log.d("RegisterViewModel", "Actualizando email: $newEmail")
        _uiState.update { it.copy(email = newEmail) }
    }

    // Actualizamos el email de confirmación
    fun updateConfirmEmail(confirmEmail: String) {
        Log.d("RegisterViewModel", "Actualizando confirmación de email: $confirmEmail")
        _uiState.update { it.copy(confirmEmail = confirmEmail) }
    }

    // Actualizamos la contraseña principal
    fun updatePassword(newPassword: String) {
        Log.d("RegisterViewModel", "Actualizando password")
        _uiState.update { it.copy(password = newPassword) }
    }

    // Actualizamos la confirmación de contraseña
    fun updateConfirmPassword(confirmPassword: String) {
        Log.d("RegisterViewModel", "Actualizando confirmación de password")
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
    }

    // Alternamos la visibilidad de la contraseña (mostrar/ocultar)
    fun togglePasswordVisibility() {
        val newValue = !_uiState.value.showPassword
        Log.d("RegisterViewModel", "Cambiando visibilidad de password a: $newValue")
        _uiState.update { it.copy(showPassword = newValue) }
    }

    /**
     * Validamos que los emails coincidan.
     * Si no coinciden, lanzamos un mensaje de error.
     */
    fun isEmailValid(): Boolean {
        val email = _uiState.value.email
        val confirmEmail = _uiState.value.confirmEmail
        val valid = email == confirmEmail && validarEmail(email ?: "")
        if (!valid) {
            Log.w("RegisterViewModel", "Los emails no coinciden o no son válidos")
            showError("Los emails no coinciden")
        } else {
            Log.d("RegisterViewModel", "Emails válidos y coinciden")
        }
        return valid
    }

    /**
     * Validamos que las contraseñas coincidan.
     * Si no coinciden, lanzamos un mensaje de error.
     */
    fun isPasswordValid(): Boolean {
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword
        val valid = password == confirmPassword && validarPassword(password)
        if (!valid) {
            Log.w("RegisterViewModel", "Las contraseñas no coinciden o no cumplen requisitos")
            showError("Las contraseñas no coinciden")
        } else {
            Log.d("RegisterViewModel", "Contraseñas válidas y coinciden")
        }
        return valid
    }

    fun enviarEvento(mensaje: String) {
        viewModelScope.launch {
            _eventos.emit(mensaje)
        }
    }

    fun showError(message: String) {
        enviarEvento("Error: $message")
    }

}
