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
import com.ajpr00.visumloop.tablet.util.validarEmail
import com.ajpr00.visumloop.tablet.util.validarPassword

@Composable
fun FromRegister(
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel,
    goToBack: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val uiStateEvent by viewModel.eventState.collectAsState()


    var isEmailOK by rememberSaveable { mutableStateOf(false) }
    var isPassOK by rememberSaveable { mutableStateOf(false) }
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
                label = { Text("Email") },
                supportingText = {
                    if (!validarEmail(uiState.email) && isEmailOK)
                        Text("Formato de email incorrecto")
                }
            )
            OutlinedTextField(
                value = uiState.confirmEmail ?: "",
                onValueChange = { viewModel.updateConfirmEmail(it) },
                label = { Text("Confirmar Email") },
            )

            Spacer(modifier = Modifier.height(12.dp))

            // PASS
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text("Contraseña") },
                singleLine = true,
                supportingText = {
                    if (!validarPassword(uiState.password) && uiState.password.length >= 8 && isPassOK)
                        Text("La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial")
                },
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
                enabled = !uiState.email.isNullOrBlank() && !uiState.confirmEmail.isNullOrBlank() && uiState.password.isNotBlank() && uiState.confirmPassword.isNotBlank(),
                icono = R.drawable.email_ic,
                label = "Registrarse",
                onClick = {
                    isEmailOK = true
                    isPassOK = true
                    if (viewModel.isEmailValid() && viewModel.isPasswordValid())
                        viewModel.comprobarYRegistrar(
                            email = uiState.email!!,
                            password = uiState.password
                        )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "¿Ya tienes una cuenta? Inicia sesión",
                modifier = Modifier.clickable { goToBack() }
            )
        }


        when (uiStateEvent) {
            is EstadoEvento.Inicial -> {
                // Mostrar formulario vacío
            }

            is EstadoEvento.Cargando -> {
                CircularProgressIndicator()
            }

            is EstadoEvento.Exito -> {
                Toast.makeText(context, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show()
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
