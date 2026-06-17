package com.ajpr00.tablet.ui.screen.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ajpr00.components.components.TextoConDivisor
import com.ajpr00.tablet.R

@Composable
fun OnboardingWelcomeScreenTablet(
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Image(
            painter = painterResource(id = R.drawable.onboardin1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .alpha(0.1f)        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier
                    .width(700.dp)
                    .aspectRatio(3f)
                    .padding(top = 40.dp),

                painter = painterResource(id = R.drawable.app_nombre_t_tulo1),
                contentDescription = "Logo"
            )
            TextoConDivisor(modifier = Modifier.padding(30.dp), texto = "¿Listo para empezar?")
            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {

                Text(
                    text = "Dale una segunda vida a tus tablets antiguas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Convierte tus dispositivos en elegantes marcos digitales para fotos y vídeos.",
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 28.sp
                )

                Text(
                    text = "Sincroniza contenido desde tu móvil, disfruta de tus recuerdos y gestiona todo de forma remota.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 28.sp
                )

                Text(
                    text = "Una solución sencilla, sostenible y diseñada para aprovechar dispositivos que ya no utilizas.",
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 28.sp
                )
            }



            Spacer(modifier = Modifier.height(32.dp))

            Button(
                modifier = Modifier.width(400.dp),
                onClick = onNext,
            ) {
                Text("Continuar")
            }
        }
    }
}
