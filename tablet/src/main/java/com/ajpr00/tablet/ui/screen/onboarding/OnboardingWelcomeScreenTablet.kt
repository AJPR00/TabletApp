package com.ajpr00.tablet.ui.screen.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingWelcomeScreenTablet(
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Bienvenido a VisumLoop",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Dale una segunda vida a tus tablets antiguas y conviértelas en elegantes marcos digitales para fotos y vídeos.\n" +
                    "\n" +
                    "Disfruta de tus recuerdos favoritos en una presentación continua, sincroniza contenido fácilmente desde tu móvil y gestiona todo de forma remota.\n" +
                    "\n" +
                    "Una solución sencilla, sostenible y diseñada para aprovechar dispositivos que ya no utilizas.",
            fontSize = 20.sp,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            modifier = Modifier.width(400.dp),
            onClick = onNext,
        ) {
            Text("Continuar")
        }
    }
}
