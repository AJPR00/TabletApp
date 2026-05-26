package com.ajpr00.mobile.qr

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors

/**
 * Crea el PreviewView(VistaClasica XML) donde CameraX va a dibujar la cámara.
 */
fun buildPreviewView(context: Context): PreviewView {
    Log.d("QR_FLOW", "🎥 buildPreviewView() → creando PreviewView")
    return PreviewView(context)
}

/**
 * setupCamera()
 * -------------
 * Aquí se inicializa CameraX.
 *
 * Flujo:
 *  1. Pedimos el CameraProvider (motor de CameraX)
 *  2. Creamos la preview (lo que se ve en pantalla)
 *  3. Creamos el analyzer (ZXing)
 *  4. Hacemos bindToLifecycle → se enciende la cámara
 */
fun setupCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onQrDetected: (String) -> Unit
) {
    Log.d("QR_FLOW", "📸 setupCamera() → solicitando CameraProvider")

    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({

        val cameraProvider = cameraProviderFuture.get()
        Log.d("QR_FLOW", "📸 CameraProvider listo")

        val preview = buildPreviewUseCase(previewView)
        val analyzer = buildAnalyzerUseCase(onQrDetected)

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            Log.d("QR_FLOW", "🔄 UnbindAll() antes de iniciar cámara")
            cameraProvider.unbindAll()

            Log.d("QR_FLOW", "📌 bindToLifecycle() → iniciando cámara")
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                analyzer
            )

            Log.d("QR_FLOW", "🚀 Cámara iniciada correctamente")

        } catch (e: Exception) {
            Log.e("QR_FLOW", "❌ Error iniciando cámara", e)
        }

    }, ContextCompat.getMainExecutor(context))
}

/**
 * buildPreviewUseCase()
 * ---------------------
 * Crea el UseCase que muestra la imagen de la cámara en pantalla.
 */
fun buildPreviewUseCase(previewView: PreviewView): Preview {
    Log.d("QR_FLOW", "🎬 buildPreviewUseCase() → configurando Preview")
    return Preview.Builder().build().also {
        it.surfaceProvider = previewView.surfaceProvider
    }
}

/**
 * buildAnalyzerUseCase()
 * ----------------------
 * Crea el UseCase que analiza cada frame con ZXing.
 *
 * Aquí es donde CameraX llama internamente a analyze() en QRAnalyzer.
 */
fun buildAnalyzerUseCase(onQrDetected: (String) -> Unit): ImageAnalysis {
    Log.d("QR_FLOW", "🔍 buildAnalyzerUseCase() → configurando Analyzer")

    return ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also {
            it.setAnalyzer(
                Executors.newSingleThreadExecutor(),
                QRXZingAnalyzer(
                    analysisIntervalMs = 240,
                    onResult = { qrText ->

                        Log.d("QR_FLOW","🎯 QR detectado en Analyzer: $qrText")

                        onQrDetected(qrText)
                    }
                )
            )
        }
}
