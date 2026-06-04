package com.ajpr00.tablet.ui.screen

import androidx.compose.runtime.mutableStateOf
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.ajpr00.tablet.R
import com.ajpr00.presentation_common.state.Estado
import com.ajpr00.presentation_common.viewmodel.RegisterViewModel
import com.ajpr00.core.util.validarEmail
import com.ajpr00.components.screen.RecoverPasswordContent
@Composable
fun RecoverPasswordTablet(
    viewModel: RegisterViewModel,
    goToBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val uiEvent by viewModel.state.collectAsState()

    var emailTouched by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { mensaje ->
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    RecoverPasswordContent(
        logo = painterResource(id = R.drawable.app_nombre_t_tulo1),
        email = uiState.email ?: "",
        onEmailChange = {
            emailTouched = true
            viewModel.updateEmail(it)
        },
        onSendClick = {
            emailTouched = true
            viewModel.recuperarPassword(uiState.email!!)
        },
        onBackClick = goToBack,
        emailError = emailTouched && !validarEmail(uiState.email),
        isLoading = uiEvent is Estado.Cargando
    )
}
