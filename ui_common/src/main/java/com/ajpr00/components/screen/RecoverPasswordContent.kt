package com.ajpr00.components.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
@Composable
fun RecoverPasswordContent(
    logo: Painter,
    email: String,
    onEmailChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onBackClick: () -> Unit,
    emailError: Boolean,
    isLoading: Boolean
) {
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

            Text("Recuperar contraseña", style = MaterialTheme.typography.headlineMedium)

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
                label = { Text("Email") },
                supportingText = {
                    if (emailError) Text("Formato de email incorrecto")
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                enabled = email.isNotBlank(),
                onClick = onSendClick
            ) {
                Text("Enviar correo de recuperación")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "¿Ya tienes una cuenta? Inicia sesión",
                modifier = Modifier.clickable { onBackClick() }
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
        }
    }
}

