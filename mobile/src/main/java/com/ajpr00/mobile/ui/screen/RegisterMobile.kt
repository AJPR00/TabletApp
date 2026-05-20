package com.ajpr00.mobile.ui.screen

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.ajpr00.mobile.presentation.state.EstadoEvento
import com.ajpr00.visumloop.mobile.R
import com.ajpr00.components.screen.RegisterContent
import com.ajpr00.presentation_common.viewmodel.RegisterViewModel

@Composable
fun RegisterMobile(
    viewModel: RegisterViewModel,
    goToBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val uiEvent by viewModel.eventState.collectAsState()

    RegisterContent(
        logo = painterResource(id = R.drawable.ic_mobile_control),
        email = uiState.email ?: "",
        confirmEmail = uiState.confirmEmail ?: "",
        password = uiState.password,
        confirmPassword = uiState.confirmPassword,
        showPassword = uiState.showPassword,
        onEmailChange = viewModel::updateEmail,
        onConfirmEmailChange = viewModel::updateConfirmEmail,
        onPasswordChange = viewModel::updatePassword,
        onConfirmPasswordChange = viewModel::updateConfirmPassword,
        onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
        onRegisterClick = viewModel::registrar,
        onBackClick = goToBack,
        loading = uiEvent is EstadoEvento.Cargando,
        messages = if (uiEvent is EstadoEvento.Mensajes)
            (uiEvent as EstadoEvento.Mensajes).mensajes
        else emptyList()
    )

    when (uiEvent) {
        is EstadoEvento.Exito -> goToBack()

        is EstadoEvento.Mensajes -> {
            (uiEvent as EstadoEvento.Mensajes).mensajes.forEach {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
            viewModel.clearErrors()
        }

        else -> Unit
    }
}
