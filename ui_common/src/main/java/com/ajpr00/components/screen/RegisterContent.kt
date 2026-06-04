package com.ajpr00.components.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegisterContent(
    logo: Painter,
    email: String,
    confirmEmail: String,
    password: String,
    confirmPassword: String,
    showPassword: Boolean,
    onEmailChange: (String) -> Unit,
    onConfirmEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit,
    loading: Boolean,
) {
    var showPassConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = logo,
                contentDescription = "Logo"
            )

            Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Atrás",
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onBackClick() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") }
            )

            OutlinedTextField(
                value = confirmEmail,
                onValueChange = onConfirmEmailChange,
                label = { Text("Confirmar Email") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (showPassword)
                    VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (showPassword)
                                Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
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

            Button(
                enabled = email.isNotBlank() &&
                        confirmEmail.isNotBlank() &&
                        password.isNotBlank() &&
                        confirmPassword.isNotBlank(),
                onClick = onRegisterClick
            ) {
                Text("Registrarse")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "¿Ya tienes una cuenta? Inicia sesión",
                modifier = Modifier.clickable { onBackClick() }
            )
        }

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
        }
    }
}
