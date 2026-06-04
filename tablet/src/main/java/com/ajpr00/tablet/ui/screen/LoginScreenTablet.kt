package com.ajpr00.tablet.ui.screen

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.ajpr00.tablet.R
import com.ajpr00.presentation_common.state.Estado
import com.ajpr00.presentation_common.viewmodel.LoginViewModel
import com.ajpr00.components.screen.LoginScreenContent


@Composable
fun LoginScreenTablet(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel,
    goToMainGraph: () -> Unit,
    goToFromRegistro: () -> Unit,
    goToRecuperarPass: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val event by viewModel.State.collectAsState()

    var isEmail by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { mensaje ->
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    LoginScreenContent(
        logo = painterResource(id = R.drawable.app_nombre_t_tulo1),
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
        loading = event is Estado.Cargando,
    )
}



