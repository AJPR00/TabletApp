package com.ajpr00.mobile.presentation.screen.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingConnectScreen(
    onTabletFound: () -> Unit,
    onBack: () -> Unit,
    requestPermissions: () -> Unit,
    permissionsGranted: Boolean,
    isSearching: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Conecta tu tablet",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Asegúrate de que tu móvil y tu tablet están en la misma red WiFi.",
            fontSize = 18.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!permissionsGranted) {
            Button(
                onClick = requestPermissions,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Conceder permisos")
            }
        } else {
            Button(
                onClick = onTabletFound, // Aquí luego llamas a tu búsqueda mDNS real
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buscar tablet")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isSearching) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Buscando tablet…")
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onBack) {
            Text("Volver")
        }
    }
}
