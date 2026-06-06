package com.ajpr00.mobile.qr.camara

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

/**
 * Inicializa CameraX y vincula los UseCases necesarios para mostrar la cámara
 * y analizar los frames en busca de códigos QR.
 *
 * Flujo:
 * 1. Obtiene el `CameraProvider`, que es el motor principal de CameraX.
 * 2. Construye el UseCase de preview (mostrar imagen).
 * 3. Construye el UseCase de análisis (ZXing).
 * 4. Llama a `bindToLifecycle` para activar la cámara.
 *
 * Arquitectura:
 * - Capa: infraestructura (módulo `qr/camera`)
 * - Rol: configurar CameraX y vincular los UseCases.
 * - No realiza lógica de dominio ni parseo; solo inicializa la cámara.
 *
 * @param context Contexto necesario para obtener el CameraProvider.
 * @param lifecycleOwner Ciclo de vida donde se vinculará la cámara.
 * @param previewView Vista donde se mostrará la imagen de la cámara.
 * @param onQrDetected Callback que recibe el texto del QR detectado.
 */
fun setupCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onQrDetected: (String) -> Unit
) {
    Log.d("QR_FLOW", "setupCamera: solicitando CameraProvider")

    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({

        val cameraProvider = cameraProviderFuture.get()
        Log.d("QR_FLOW", "setupCamera: CameraProvider listo")

        val preview = buildPreviewUseCase(previewView)
        val analyzer = buildAnalyzerUseCase(onQrDetected)
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            Log.d("QR_FLOW", "setupCamera: unbindAll antes de iniciar cámara")
            cameraProvider.unbindAll()

            Log.d("QR_FLOW", "setupCamera: bindToLifecycle iniciando cámara")
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                analyzer
            )

            Log.d("QR_FLOW", "setupCamera: cámara iniciada correctamente")

        } catch (e: Exception) {
            Log.e("QR_FLOW", "setupCamera: error iniciando cámara", e)
        }

    }, ContextCompat.getMainExecutor(context))
}