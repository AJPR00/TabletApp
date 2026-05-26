package com.ajpr00.mobile.ui.screen

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import com.ajpr00.mobile.presentation.viewmodel.RegisterDeviceViewModel
import com.ajpr00.visumloop.mobile.R

@Composable
fun RegisterDeviceScreen(
    viewModel: RegisterDeviceViewModel,
    onScanQR: () -> Unit,
    onRegistrar: () -> Unit,
    onCancelar: () -> Unit
) {

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onScanQR()
        } else {
            Log.e("QR", "❌ Permiso de cámara DENEGADO")
        }
    }
    val state by viewModel.uiState.collectAsState()

    Dialog(onDismissRequest = onCancelar) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Registrar Dispositivo",
                    style = MaterialTheme.typography.headlineSmall
                )

                Button(
                    onClick = {viewModel.showBuscarDialog(true)},
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Buscar dispositivo") }

                Button(
                    onClick = { launcher.launch(android.Manifest.permission.CAMERA) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Leer QR del dispositivo") }

                Button(
                    onClick = viewModel::onToggleManualFields,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Introducir manualmente")
                }

                if (state.showManualFields) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.nombre,
                            placeholder = { Text("Nombre del dispositivo") },
                            singleLine = true,
                            onValueChange = viewModel::onNombreChange
                        )

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.ip,
                            singleLine = true,
                            onValueChange = viewModel::onIpChange,
                            label = { Text("IP del dispositivo") },
                            leadingIcon = {
                                Row(
                                    modifier = Modifier.padding(end = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        modifier = Modifier.size(24.dp),
                                        painter = painterResource(id = R.drawable.ic_introducir_ip),
                                        contentDescription = null
                                    )
                                    Text("IP:")
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    OutlinedButton(
                        onClick = {
                            viewModel.reset()
                            onCancelar()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancelar") }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = onRegistrar,
                        enabled = state.isValid,
                        modifier = Modifier.weight(1f)
                    ) { Text("Registrar") }
                }
            }
        }
    }
}
