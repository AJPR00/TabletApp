package com.ajpr00.mobile.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ajpr00.visumloop.mobile.R
import com.ajpr00.presentation_common.state.EstadoEvento
import com.ajpr00.presentation_common.viewmodel.LoginViewModel
import com.ajpr00.components.components.CustomButtonLogin
import com.ajpr00.components.screen.LoginScreenContent


@Composable
fun LoginScreenMobile(
    viewModel: LoginViewModel,
    goToMainGraph: () -> Unit,
    goToFromRegistro: () -> Unit,
    goToRecuperarPass: () -> Unit,
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val event by viewModel.eventState.collectAsState()

    var isEmail by rememberSaveable { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LoginScreenContent(
            modifier = Modifier.fillMaxWidth(1f).fillMaxHeight(0.9f),
            logo = painterResource(id = R.drawable.imag_splash_control),
            email = state.email ?: "",
            password = state.password,
            showPassword = state.showPassword,
            isEmailMode = isEmail,
            onEmailModeToggle = { isEmail = !isEmail },
            onEmailChange = viewModel::updateEmail,
            onPasswordChange = viewModel::updatePassword,
            onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
            onLoginWithGoogle = { viewModel.onGoogleLoginClick() },
            onLoginWithFacebook = { /* ... */ },
            onLoginWithEmail = { /* ... */ },
            onGoToRegister = goToFromRegistro,
            onGoToRecoverPassword = goToRecuperarPass,
            onExit = goToMainGraph,
            loading = event is EstadoEvento.Cargando,
            messages = if (event is EstadoEvento.Mensajes) (event as EstadoEvento.Mensajes).mensajes else emptyList()
        )
        HorizontalDivider(modifier = Modifier.padding(16.dp))
        CustomButtonLogin(
            icono = R.drawable.ic_acceso_como_local,
            label = "Acceder Local",
            onClick = { }
        )

    }
}

