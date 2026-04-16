package com.ajpr00.visumloop.tablet.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ajpr00.visumloop.tablet.R
import com.ajpr00.visumloop.tablet.presentation.state.EstadoEvento
import com.ajpr00.visumloop.tablet.presentation.viewmodel.RegisterViewModel
import com.ajpr00.visumloop.tablet.ui.components.CustomButton
import com.ajpr00.visumloop.tablet.util.validarEmail

@Composable
fun FromRecover(
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel,
    goToBack: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val uiStateEvent by viewModel.eventState.collectAsState()

    var isEmailOK by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_nombre_t_tulo1),
                contentDescription = "Logo"
            )

            Text("Recuperar contraseña", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Atrás",
                modifier = Modifier
                    .padding(end = 100.dp)
                    .clickable { goToBack() }
                    .align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // EMAIL
            OutlinedTextField(
                value = uiState.email ?: "",
                onValueChange = { viewModel.updateEmail(it) },
                label = { Text("Email") },
                supportingText = {
                    if (!validarEmail(uiState.email) && isEmailOK)
                        Text("Formato de email incorrecto")
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            CustomButton(
                enabled = !uiState.email.isNullOrBlank(),
                icono = R.drawable.email_ic,
                label = "Enviar correo de recuperación",
                onClick = {
                    isEmailOK = true
                    viewModel.recuperarPassword(uiState.email!!)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "¿Ya tienes una cuenta? Inicia sesión",
                modifier = Modifier.clickable { goToBack() }
            )
        }

        when (uiStateEvent) {
            is EstadoEvento.Inicial -> Unit

            is EstadoEvento.Cargando -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
            }

            is EstadoEvento.Exito -> {
                Toast.makeText(context, "Correo enviado correctamente", Toast.LENGTH_SHORT).show()
                goToBack()
            }

            is EstadoEvento.Mensajes -> {
                val errores = (uiStateEvent as EstadoEvento.Mensajes).mensajes
                errores.forEach { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
                viewModel.clearErrors()
            }
        }
    }
}
