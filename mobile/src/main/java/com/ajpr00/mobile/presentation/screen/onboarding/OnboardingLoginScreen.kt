package com.ajpr00.mobile.presentation.screen.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingLoginScreen(
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Antes de continuar",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Necesitamos algunos permisos para que VisumLoop funcione correctamente:",
            fontSize = 18.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("• Acceso a red para detectar tu tablet")
        Text("• Almacenamiento para enviar archivos")
        Text("• Cámara (opcional) para escanear QR")

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onBack) {
            Text("Volver")
        }
    }
}
