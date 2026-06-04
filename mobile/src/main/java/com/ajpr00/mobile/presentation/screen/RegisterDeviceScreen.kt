package com.ajpr00.mobile.presentation.screen

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import com.ajpr00.components.components.DynamicButton
import com.ajpr00.components.components.showToast
import com.ajpr00.mobile.presentation.viewmodel.RegisterDeviceViewModel
import com.ajpr00.mobile.ui.components.DiscoveredDeviceCard
import com.ajpr00.mobile.ui.mapper.toDispositivoUi
import com.ajpr00.uicommon.R


@Composable
fun RegisterDeviceScreen(
    viewModelRegistrerDevice: RegisterDeviceViewModel,
    onScanQR: () -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current

    val state by viewModelRegistrerDevice.uiState.collectAsState()
    var selectedId: String? by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    LaunchedEffect(state.foundDevices, state.showListManualLan) {
        snapshotFlow { scrollState.maxValue }.collect { max ->
            scrollState.scrollTo(max)
        }
    }

    LaunchedEffect(Unit) {
        viewModelRegistrerDevice.eventos.collect { mensaje ->
            showToast(context, mensaje)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onScanQR()
        } else {
            Log.e("QR", "Permiso de cámara DENEGADO")
        }
    }

    Dialog(onDismissRequest = onCancelar) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(scrollState)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Registrar Dispositivo",
                    style = MaterialTheme.typography.headlineSmall
                )

                DynamicButton(
                    isLoading = state.isSearchingAut,
                    isEnabled = !state.isSearchingAut,
                    text = "Buscar dispositivo",
                    loadingText = "Buscando…",
                    onClick = {
                        viewModelRegistrerDevice.showBuscarDialog(true)
                        viewModelRegistrerDevice.buscarAutoEnRed()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.showListAutoLan) {
                    Log.d("RegisterDeviceScreen", "Mostrando lista de dispositivos")
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(state.foundDevices.size) { index->
                            val dispositivo = state.foundDevices[index]

                            Log.d("RegisterDeviceScreen", "Item[$index] → nombre=${dispositivo.nombre}, id=${dispositivo.id}")

                            Log.d(
                                "RegisterDeviceScreen",
                                "Comparando selectedId=$selectedId con dispositivo.id=${dispositivo.id} → isSelected=${selectedId == dispositivo.id}"
                            )
                            DiscoveredDeviceCard(
                                dispositivo = dispositivo.toDispositivoUi(),
                                isSelected = selectedId == dispositivo.id,

                                onClick = {
                                    Log.d("RegisterDeviceScreen", "CLICK → ${dispositivo.nombre} (id=${dispositivo.id})")
                                    selectedId = if (selectedId == dispositivo.id) {
                                        Log.d("RegisterDeviceScreen", "Deseleccionando dispositivo ${dispositivo.nombre}")
                                        viewModelRegistrerDevice.setCurrentDevice(null)
                                        null
                                    } else {
                                        Log.d("RegisterDeviceScreen", "Seleccionando dispositivo ${dispositivo.nombre}")
                                        viewModelRegistrerDevice.setCurrentDevice(dispositivo)
                                        dispositivo.id
                                    }
                                    Log.d("RegisterDeviceScreen", "selectedId ahora es: $selectedId")
                                }
                            )
                        }
                    }
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        launcher.launch(Manifest.permission.CAMERA)
                    }
                ) { Text("Escanear QR") }


                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModelRegistrerDevice.showBuscarDialog(false)
                        viewModelRegistrerDevice.isSearchingAut(false)
                        viewModelRegistrerDevice.onToggleManualFields()
                    }
                ) { Text("Introducir manualmente") }

                if (state.showManualFields) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        /*OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.nombre,
                            placeholder = { Text("Nombre del dispositivo") },
                            singleLine = true,
                            onValueChange = viewModel::onNombreChange
                        )*/

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.ip,
                            singleLine = true,
                            onValueChange = viewModelRegistrerDevice::onIpChange,
                            label = {
                                Text(
                                    "IP del dispositivo",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = {
                                    Text("IP:")
                            },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.outline_search_24), // tu icono
                                    contentDescription = "Buscar",
                                    modifier = Modifier
                                        .size(28.dp)
                                        .padding(end = 4.dp)
                                        .clickable {
                                            viewModelRegistrerDevice.buscarManual()
                                        }
                                )
                            }
                        )
                        if (state.showListManualLan && state.foundDevices.isNotEmpty()) {
                            DiscoveredDeviceCard(
                                dispositivo = state.foundDevices[0].toDispositivoUi(),
                                isSelected = selectedId == state.foundDevices[0].id,
                                onClick = {
                                    selectedId = if (selectedId == state.foundDevices[0].id) {
                                        viewModelRegistrerDevice.setCurrentDevice(null)
                                        null
                                    } else {
                                        viewModelRegistrerDevice.setCurrentDevice(state.foundDevices[0])
                                        state.foundDevices[0].id
                                    }
                                }
                            )
                        }

                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    OutlinedButton(
                        onClick = {
                            viewModelRegistrerDevice.reset()
                            onCancelar()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancelar") }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = viewModelRegistrerDevice::registrarDispositivo,
                        enabled = state.isValid,
                        modifier = Modifier.weight(1f)
                    ) { Text("Registrar") }
                }
            }
        }
    }
}
