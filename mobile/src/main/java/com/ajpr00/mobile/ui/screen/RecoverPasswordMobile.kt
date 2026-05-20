package com.ajpr00.mobile.ui.screen

import android.widget.Toast
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.ajpr00.visumloop.mobile.R
import com.ajpr00.mobile.presentation.state.EstadoEvento
import com.ajpr00.components.screen.RecoverPasswordContent
import com.ajpr00.core.util.validarEmail
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.ajpr00.presentation_common.viewmodel.RegisterViewModel

@Composable
fun RecoverPasswordMobile(
    viewModel: RegisterViewModel,
    goToBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val uiEvent by viewModel.eventState.collectAsState()

    var emailTouched by rememberSaveable { mutableStateOf(false) }

    RecoverPasswordContent(
        logo = painterResource(id = R.drawable.ic_mobile_control),
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
        isLoading = uiEvent is EstadoEvento.Cargando
    )

    when (uiEvent) {
        is EstadoEvento.Exito -> {
            Toast.makeText(context, "Correo enviado correctamente", Toast.LENGTH_SHORT).show()
            goToBack()
        }

        is EstadoEvento.Mensajes -> {
            (uiEvent as EstadoEvento.Mensajes).mensajes.forEach {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
            viewModel.clearErrors()
        }

        else -> Unit
    }
}