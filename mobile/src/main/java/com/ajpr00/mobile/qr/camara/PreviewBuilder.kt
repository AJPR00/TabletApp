package com.ajpr00.mobile.qr.camara

import android.content.Context
import android.util.Log
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView

/**
 * Crea el `PreviewView` donde CameraX dibuja la imagen de la cámara.
 *
 * Este componente actúa como el contenedor visual del flujo de vídeo.
 * No realiza lógica de cámara; solo expone un `surfaceProvider` que
 * otros UseCases (como Preview) utilizarán para renderizar.
 *
 * Arquitectura:
 * - Capa: infraestructura (módulo `qr/camera`)
 * - Rol: proporcionar la vista donde se mostrará la cámara.
 *
 * @param context Contexto necesario para instanciar la vista.
 * @return Un `PreviewView` listo para usarse en la UI.
 */
fun buildPreviewView(context: Context): PreviewView {
    Log.d("QR_FLOW", "buildPreviewView: creando PreviewView")
    return PreviewView(context)
}

/**
 * Crea el UseCase encargado de mostrar la imagen de la cámara en pantalla.
 *
 * Este UseCase recibe los frames de CameraX y los dibuja en el `PreviewView`
 * mediante su `surfaceProvider`. No analiza frames ni interactúa con ZXing.
 *
 * Flujo:
 * 1. Se construye un `Preview`.
 * 2. Se asigna el `surfaceProvider` del `PreviewView`.
 * 3. CameraX renderiza la imagen directamente en la vista.
 *
 * Arquitectura:
 * - Capa: infraestructura (módulo `qr/camera`)
 * - Rol: mostrar la imagen de la cámara.
 *
 * @param previewView Vista donde se renderizará la cámara.
 * @return UseCase `Preview` configurado.
 */
fun buildPreviewUseCase(previewView: PreviewView): Preview {
    Log.d("QR_FLOW", "buildPreviewUseCase: configurando Preview")

    return Preview.Builder()
        .build()
        .also { preview ->
            preview.surfaceProvider = previewView.surfaceProvider
            Log.d("QR_FLOW", "buildPreviewUseCase: surfaceProvider asignado")
        }
}
