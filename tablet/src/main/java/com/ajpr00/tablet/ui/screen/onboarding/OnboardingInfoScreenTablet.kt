package com.ajpr00.tablet.ui.screen.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ajpr00.tablet.R

@Composable
fun OnboardingInfoScreenTablet(
    onNext: (String) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.onboardin2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .alpha(0.10f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Configura tu tablet",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Dale un nombre a tu tablet",
                fontSize = 18.sp,
                lineHeight = 24.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Tu tablet vuelve a tener vida.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                modifier = Modifier.width(400.dp),
                value = name,
                maxLines = 1,
                onValueChange = { name = it },
                supportingText = {
                    Text("Así aparecerá en VisumControl cuando envíes fotos y vídeos.")
                },
                label = { Text("Nombre de la tablet") }
            )

            Spacer(Modifier.height(32.dp))

            Button(
                modifier = Modifier.width(400.dp),
                onClick = { if (name.isNotBlank()) onNext(name) }
            ) {
                Text("Continuar")
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onBack) {
                Text("Volver")
            }
        }
    }
}