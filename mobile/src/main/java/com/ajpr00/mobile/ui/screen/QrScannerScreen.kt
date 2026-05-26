package com.ajpr00.mobile.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ajpr00.core.domain.model.TabletConnectionData
import com.ajpr00.mobile.presentation.viewmodel.QrScannerViewModel
import com.ajpr00.mobile.qr.buildPreviewView
import com.ajpr00.mobile.qr.setupCamera

/**
 * QrScannerScreen
 * ----------------
 * Esta pantalla es la encargada de mostrar un DIALOG con la cámara activa.
 *
 * Aquí ocurre la magia:
 *  - Se crea un PreviewView (vista nativa donde CameraX dibuja la cámara)
 *  - Se inicializa CameraX
 *  - Se conecta CameraX con nuestro QRAnalyzer
 *  - Cuando ZXing detecta un QR → el ViewModel recibe los datos
 *
 * Piensa en esto como:
 *  CameraX = el que hace fotos
 *  QRAnalyzer = el que mira cada foto
 *  ZXing = el que interpreta el QR
 *  ViewModel = el que procesa el texto del QR
 */
@Composable
fun QrScannerScreen(
    viewModel: QrScannerViewModel,
    onConnectionReady: (TabletConnectionData) -> Unit,
    onCancel: () -> Unit
) {
    Log.d("QR_UI", "📱 Entrando en QrScannerScreen")

    val lifecycleOwner = LocalLifecycleOwner.current

    // Observamos el StateFlow del ViewModel.
    // Si ya tenemos datos → cerramos el dialog y seguimos el flujo.
    val connection = viewModel.connectionData.collectAsState().value
    connection?.let {
        Log.d("QR_UI", "✅ Datos QR recibidos desde ViewModel: $it")
        onConnectionReady(it)
    }

    Dialog(onDismissRequest = {
        Log.d("QR_UI", "❌ Dialog cerrado por el usuario")
        onCancel()
    }) {

        Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 8.dp,
            modifier = Modifier.padding(16.dp).size(340.dp)
        ) {

            Box(
                modifier = Modifier.fillMaxSize().padding(50.dp),
                contentAlignment = Alignment.Center
            ) {

                /**
                 * AndroidView nos permite meter una vista nativa dentro de Compose.
                 * Aquí metemos un PreviewView, que es donde CameraX dibuja la cámara.
                 */
                AndroidView(
                    modifier = Modifier.fillMaxSize().padding(bottom = 50.dp),
                    factory = { ctx ->

                        Log.d("QR_UI", "🎥 Creando PreviewView")
                        val previewView = buildPreviewView(ctx)

                        Log.d("QR_UI", "⚙️ Iniciando configuración de CameraX")
                        setupCamera(
                            context = ctx,
                            lifecycleOwner = lifecycleOwner,
                            previewView = previewView,
                            onQrDetected = { qr ->
                                Log.d("QR_UI", "🎯 QR detectado desde Analyzer: $qr")
                                viewModel.onQrDetected(qr)
                            }
                        )
                        previewView
                    }
                )
                OutlinedButton(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    onClick = {
                        viewModel.reset()
                        onCancel()
                    }
                ) { Text("Cancelar") }
            }
        }
    }
}