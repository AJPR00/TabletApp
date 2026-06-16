package com.ajpr00.tablet.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ajpr00.components.components.TextoConDivisor
import com.ajpr00.tablet.presentation.viewmodel.SettingsViewModel
import com.ajpr00.tablet.R
import com.ajpr00.tablet.presentation.qr.QrGenerator
import com.google.gson.Gson
import kotlinx.coroutines.delay

/**
 * # SettingsScreen
 *
 * Pantalla de configuración principal de la tablet.
 *
 * ## Funcionalidades
 * - Cambiar modo oscuro
 * - Cambiar idioma
 * - Editar nombre de la tablet
 * - Mostrar ID de la tablet
 * - Mostrar estado de conexión con el móvil
 * - Regenerar QR
 * - Borrar clave AES (desvincular móvil)
 *
 * ## Arquitectura
 * - **UI**: Compose
 * - **State**: `SettingsState` expuesto por `SettingsViewModel`
 * - **Eventos**: llamadas directas a métodos del ViewModel
 *
 * ## Notas
 * - La UI es completamente reactiva gracias a `collectAsState()`
 * - Los cambios se reflejan automáticamente al modificar DataStore
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val state by viewModel.state.collectAsState()
    val qrPayload by viewModel.qrPayload.collectAsState()

    // ⭐ Estado del ViewModel (NO local)
    val showQrDialog by viewModel.showQrDialog.collectAsState()
    val remaining by viewModel.qrTimer.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.TopCenter),
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            modifier = Modifier.align(Alignment.TopEnd),
            text = if (state.isMobileConnected)
                stringResource(R.string.mobile_connected, state.mobileName)
            else
                stringResource(R.string.mobile_disconnected)
        )

        Column(
            modifier = Modifier
                .width(500.dp)
                .padding(top = 50.dp)
                .align(Alignment.TopStart)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {

            // ---------------------------------------------------------
            // MODO OSCURO
            // ---------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.dark_mode))
                Switch(
                    checked = state.isDarkMode,
                    onCheckedChange = { viewModel.toggleDarkMode() }
                )
            }

            // ---------------------------------------------------------
            // IDIOMA
            // ---------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.language),
                    style = MaterialTheme.typography.titleMedium
                )

                var expanded by remember { mutableStateOf(false) }

                Box {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text(state.language.uppercase())
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("ES") },
                            onClick = {
                                expanded = false
                                viewModel.changeLanguage("es")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("EN") },
                            onClick = {
                                expanded = false
                                viewModel.changeLanguage("en")
                            }
                        )
                    }
                }
            }

            TextoConDivisor(texto = stringResource(R.string.tablet_identity))

            // ---------------------------------------------------------
            // IDENTIDAD DE LA TABLET
            // ---------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.tablet_name_label),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(state.tabletName, style = MaterialTheme.typography.bodySmall)
            }

            Text(
                text = stringResource(R.string.tablet_id_label) + " ${state.tabletId}",
                style = MaterialTheme.typography.bodySmall
            )

            // ---------------------------------------------------------
            // ACCIONES
            // ---------------------------------------------------------
            Text(
                text = stringResource(R.string.actions),
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = {
                    viewModel.regenerateQr()   // ⭐ Esto ya inicia el temporizador
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.regenerate_qr))
            }

            Button(
                onClick = viewModel::clearAesKey,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.clear_aes_key))
            }

            // ---------------------------------------------------------
            // DIÁLOGO QR
            // ---------------------------------------------------------
            if (showQrDialog) {
                Dialog(onDismissRequest = { viewModel.closeQrDialog() }) {

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 6.dp,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "QR generado",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            qrPayload?.let { payload ->
                                val qrJson = Gson().toJson(payload)

                                QrGenerator(
                                    data = qrJson,
                                    modifier = Modifier.size(260.dp)
                                )
                            } ?: Text("Generando QR…")

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Tiempo restante: $remaining s",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            Button(
                                onClick = { viewModel.closeQrDialog() },
                                modifier = Modifier.width(150.dp)
                            ) {
                                Text("Cerrar")
                            }
                        }
                    }
                }
            }
        }
    }
}