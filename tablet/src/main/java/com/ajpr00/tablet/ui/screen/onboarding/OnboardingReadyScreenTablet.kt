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
fun OnboardingReadyScreenTablet(
    tabletName: String,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Todo listo",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Esta tablet se identificará como:",
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "“$tabletName”",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tu tablet está lista para recibir contenido.\n" +
                    "Abre la app en tu móvil y conéctate.",
            fontSize = 18.sp,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            modifier = Modifier.width(400.dp),
            onClick = onFinish,
        ) {
            Text("Empezar")
        }
    }
}
