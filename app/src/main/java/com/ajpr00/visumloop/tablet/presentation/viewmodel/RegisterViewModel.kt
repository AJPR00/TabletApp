package com.ajpr00.visumloop.tablet.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ajpr00.visumloop.tablet.data.datasource.local.preferences.LoginPreferences
import com.ajpr00.visumloop.tablet.data.repository.LoginRepository
import com.ajpr00.visumloop.tablet.presentation.state.EstadoEvento
import com.ajpr00.visumloop.tablet.presentation.state.RegisterState
import com.ajpr00.visumloop.tablet.util.validarEmail
import com.ajpr00.visumloop.tablet.util.validarPassword
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: LoginRepository,
    private val loginPreferences: LoginPreferences
) : ViewModel() {

    // Estado principal del registro: aquí guardamos email, password, confirmaciones, etc.
    private val _uiState = MutableStateFlow(RegisterState())
    val uiState: StateFlow<RegisterState> = _uiState

    private val _eventState = MutableStateFlow<EstadoEvento>(EstadoEvento.Inicial)
    val eventState: StateFlow<EstadoEvento> = _eventState


    /**
     * Función que intenta registrar un usuario con Firebase usando email y password.
     * Si todo va bien, emitimos un mensaje de éxito. Si falla, emitimos un mensaje de error.
     */
    fun registrarEmailPass(email: String, password: String) {
        Log.d("RegisterViewModel", "Intentando registrar usuario con email: $email")
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("RegisterViewModel", "✅ Registro exitoso en Firebase")
                    addEvento("Cuenta creada correctamente")
                } else {
                    Log.e("RegisterViewModel", "❌ Error al registrar: ${task.exception?.message}")
                    addEvento("Error al registrar usuario")
                }
            }
    }

    fun comprobarYRegistrar(email: String, password: String) {
        val auth = FirebaseAuth.getInstance()
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        registrarEmailPass(email, password)
                    } else {
                        // Ya existe → mensaje para la UI
                        addEvento("El usuario ya existe, inicia sesión o recupera tu contraseña")
                    }
                } else {
                    addEvento("Error comprobando existencia de usuario")
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
            addEvento("Los emails no coinciden")
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
            addEvento("Las contraseñas no coinciden")
        } else {
            Log.d("RegisterViewModel", "Contraseñas válidas y coinciden")
        }
        return valid
    }
}
