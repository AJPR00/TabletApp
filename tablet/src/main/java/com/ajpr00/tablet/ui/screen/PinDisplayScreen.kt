package com.ajpr00.tablet.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

/**
 * # PinDialog
 *
 * Dialog que actúa como **overlay temporal** para mostrar el PIN de emparejamiento.
 * No cambia de pantalla, no altera la navegación y desaparece automáticamente.
 *
 * ## ¿Qué hace?
 * - Muestra el PIN en grande.
 * - Inicia un temporizador regresivo.
 * - Si el tiempo llega a 0 → se cierra solo.
 * - Si el usuario pulsa "Cerrar" → se cierra.
 * - Si `isPaired == true` (el móvil validó el PIN) → se cierra automáticamente.
 *
 * ## Cuándo usarlo
 * - Cuando quieres mostrar el PIN como overlay sin navegar.
 * - Cuando el usuario debe seguir en la misma pantalla.
 * - Cuando el PIN es un paso temporal del proceso de vinculación.
 *
 * @param pin PIN generado por el servidor.
 * @param isPaired Indica si el móvil ya validó el PIN.
 * @param durationSeconds Duración del temporizador.
 * @param onDismiss Acción al cerrar el Dialog (timeout, manual o pairing).
 */
@Composable
fun PinDialog(
    pin: String,
    isPaired: Boolean,
    durationSeconds: Int = 30,
    onDismiss: () -> Unit
) {
    var remaining by remember { mutableStateOf(durationSeconds) }

    // Temporizador
    LaunchedEffect(Unit) {
        while (remaining > 0) {
            delay(1000)
            remaining--
            Log.d("PinDialog", "Tiempo restante: $remaining")
        }
        onDismiss()
    }

    // Cierre automático si el móvil valida el PIN
    LaunchedEffect(isPaired) {
        if (isPaired) {
            Log.d("PinDialog", "PIN validado → cerrando Dialog")
            onDismiss()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    text = "Introduce este PIN en tu móvil",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = pin,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Tiempo restante: $remaining s",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Button(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        }
    }
}
