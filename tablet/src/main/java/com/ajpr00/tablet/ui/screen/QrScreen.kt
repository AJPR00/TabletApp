package com.ajpr00.tablet.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ajpr00.core.domain.model.qr.QrPayload
import com.ajpr00.tablet.presentation.qr.QrGenerator
import com.ajpr00.tablet.presentation.state.PairingState


/**
 * # QrScreen
 *
 * Pantalla encargada de mostrar el QR generado a partir del [QrPayload].
 *
 * ## Flujo
 * - Observa `qrPayload` del ViewModel.
 * - Si está cargando → muestra un loader.
 * - Si hay error → muestra mensaje de error.
 * - Si hay payload → genera el QR y lo muestra.
 *
 * ## Parámetros
 * @param uiState Estado general de la UI (loading, error…)
 * @param payload Datos necesarios para construir el QR.
 * @param onGenerateQr Acción para regenerar el QR.
 */
@Composable
fun QrScreen(
    uiState: PairingState,
    payload: QrPayload?,
    onGenerateQr: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // LOADING
        if (uiState.isLoading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Generando QR…")
            return
        }

        // ERROR
        uiState.error?.let { error ->
            Text(
                text = "Error: $error",
                color = Color.Red
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onGenerateQr) {
                Text("Reintentar")
            }
            return
        }

        // QR DISPONIBLE
        payload?.let { data ->
            Text(
                text = "Escanea este QR desde el móvil",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(24.dp))

            // Generamos el QR a partir del JSON
            val json = remember(data) {
                """
                {
                  "pin": "${data.pin}",
                  "salt": "${data.salt}",
                  "id": "${data.id}",
                  "nombre": "${data.nombre}",
                  "ip": "${data.ip}",
                  "puerto": ${data.puerto}
                }
                """.trimIndent()
            }

            QrGenerator(
                data = json,
                modifier = Modifier.size(260.dp)
            )

            Spacer(Modifier.height(24.dp))

            Button(onClick = onGenerateQr) {
                Text("Generar nuevo QR")
            }
        }
    }
}
