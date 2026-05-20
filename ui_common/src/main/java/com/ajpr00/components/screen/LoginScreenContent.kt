package com.ajpr00.components.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.ajpr00.components.components.CustomButtonLogin
import com.ajpr00.uicommon.R


@Composable
fun LoginScreenContent(
    modifier: Modifier = Modifier,
    loading: Boolean,
    logo: Painter,
    email: String,
    password: String,
    showPassword: Boolean,
    isEmailMode: Boolean,
    messages: List<String>,
    onEmailModeToggle: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginWithGoogle: () -> Unit,
    onLoginWithFacebook: () -> Unit,
    onLoginWithEmail: () -> Unit,
    onGoToRegister: () -> Unit,
    onGoToRecoverPassword: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                modifier = Modifier.fillMaxSize(0.5f),
                painter = logo,
                contentDescription = "Logo"
            )

            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!isEmailMode) {
                CustomButtonLogin(
                    icono = R.drawable.google_logo,
                    label = "Login con Google",
                    onClick = onLoginWithGoogle
                )

                Spacer(modifier = Modifier.height(24.dp))

                CustomButtonLogin(
                    icono = R.drawable.facebook_logo,
                    label = "Login con Facebook",
                    onClick = onLoginWithFacebook
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEmailMode) {
                Text(
                    "Atrás",
                    modifier = Modifier
                        .padding(end = 100.dp)
                        .clickable { onEmailModeToggle() }
                        .align(Alignment.End)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") }
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            CustomButtonLogin(
                enabled = if (isEmailMode) (email.isNotBlank() && password.isNotBlank()) else true,
                icono = R.drawable.email_ic,
                label = if (!isEmailMode) "Iniciar sesión con Email" else "Iniciar sesión",
                onClick = {
                    if (!isEmailMode) onEmailModeToggle()
                    else onLoginWithEmail()
                }
            )

            if (isEmailMode) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("¿No tienes una cuenta? Regístrate",
                    modifier = Modifier.clickable { onGoToRegister() })
                Text("¿Olvidaste tu contraseña?",
                    modifier = Modifier.clickable { onGoToRecoverPassword() })
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Salir",
                modifier = Modifier.clickable { onExit() }
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



