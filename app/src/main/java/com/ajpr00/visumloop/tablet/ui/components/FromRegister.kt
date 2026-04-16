package com.ajpr00.visumloop.tablet.ui.components


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@Composable
fun FromRegister(
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel,
    goToBack: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val uiEvent by viewModel.eventState.collectAsState()

    var showPassConfirm by rememberSaveable { mutableStateOf(false) }

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

            Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)

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
                label = { Text("Email") }
            )

            OutlinedTextField(
                value = uiState.confirmEmail ?: "",
                onValueChange = { viewModel.updateConfirmEmail(it) },
                label = { Text("Confirmar Email") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // PASS
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (uiState.showPassword)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                        Icon(
                            imageVector = if (uiState.showPassword)
                                Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // PASS CONFIRM
            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = { viewModel.updateConfirmPassword(it) },
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                visualTransformation = if (showPassConfirm)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassConfirm = !showPassConfirm }) {
                        Icon(
                            imageVector = if (showPassConfirm)
                                Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            CustomButton(
                enabled = uiState.email?.isNotBlank() == true &&
                        uiState.confirmEmail?.isNotBlank() == true &&
                        uiState.password.isNotBlank() &&
                        uiState.confirmPassword.isNotBlank(),
                icono = R.drawable.email_ic,
                label = "Registrarse",
                onClick = { viewModel.registrar() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "¿Ya tienes una cuenta? Inicia sesión",
                modifier = Modifier.clickable { goToBack() }
            )
        }

        when (uiEvent) {
            is EstadoEvento.Inicial -> Unit

            is EstadoEvento.Cargando -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
            }

            is EstadoEvento.Exito -> {
                goToBack()
            }

            is EstadoEvento.Mensajes -> {
                val mensajes = (uiEvent as EstadoEvento.Mensajes).mensajes
                mensajes.forEach { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
                viewModel.clearErrors()
            }
        }
    }
}

