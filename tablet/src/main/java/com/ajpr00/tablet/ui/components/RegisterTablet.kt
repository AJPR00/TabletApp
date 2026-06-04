package com.ajpr00.tablet.ui.components

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.ajpr00.presentation_common.viewmodel.RegisterViewModel
import com.ajpr00.components.screen.RegisterContent
import com.ajpr00.presentation_common.state.Estado
import com.ajpr00.tablet.R

@Composable
fun RegisterTablet(
    viewModel: RegisterViewModel,
    goToBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val uiEvent by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { mensaje ->
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    RegisterContent(
        logo = painterResource(id = R.drawable.app_nombre_t_tulo1 ),
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
        loading = uiEvent is Estado.Cargando,
    )
}
