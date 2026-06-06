package com.ajpr00.mobile.qr.camara

import android.util.Log
import androidx.camera.core.ImageAnalysis
import com.ajpr00.mobile.qr.analyzer.QRXZingAnalyzer
import java.util.concurrent.Executors
/**
 * Crea el UseCase encargado de analizar cada frame de la cámara.
 *
 * Este UseCase conecta CameraX con `QRXZingAnalyzer`, que es quien realiza
 * el procesamiento real del frame (copiar plano Y, rotar, pasar a ZXing,
 * aplicar throttling y cooldown).
 *
 * Arquitectura:
 * - Capa: infraestructura (módulo `qr/camera`)
 * - Rol: preparar el análisis de frames
 * - No realiza parseo ni lógica de dominio; solo delega detección.
 *
 * Flujo:
 * 1. Se crea un `ImageAnalysis` con estrategia KEEP_ONLY_LATEST para evitar colas.
 * 2. Se asigna un analizador ejecutado en un hilo independiente.
 * 3. `QRXZingAnalyzer` procesa el frame y, si detecta un QR, llama a `onQrDetected`.
 *
 * @param onQrDetected Callback que recibe el texto del QR detectado.
 * @return UseCase configurado para análisis de frames.
 */
fun buildAnalyzerUseCase(onQrDetected: (String) -> Unit): ImageAnalysis {
    Log.d("QR_FLOW", "buildAnalyzerUseCase: iniciando configuración del UseCase")

    return ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also { analysis ->

            Log.d("QR_FLOW", "buildAnalyzerUseCase: asignando executor y analizador ZXing")

            analysis.setAnalyzer(
                Executors.newSingleThreadExecutor(),
                QRXZingAnalyzer(
                    analysisIntervalMs = 240,
                    onResult = { qrText ->
                        Log.d("QR_FLOW", "buildAnalyzerUseCase: QR detectado → $qrText")
                        onQrDetected(qrText)
                    }
                )
            )

            Log.d("QR_FLOW", "buildAnalyzerUseCase: Analyzer configurado correctamente")
        }
}