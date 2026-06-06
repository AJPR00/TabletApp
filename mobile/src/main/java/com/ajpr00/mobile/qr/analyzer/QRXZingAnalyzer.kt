package com.ajpr00.mobile.qr.analyzer

import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.ChecksumException
import com.google.zxing.DecodeHintType
import com.google.zxing.FormatException
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

/**
 * Analizador de frames para lectura de códigos QR usando ZXing.
 *
 * Este analizador recibe cada frame desde CameraX y realiza el proceso completo:
 * - Extraer el plano Y (luminancia) del frame.
 * - Ajustar la rotación según la orientación del dispositivo.
 * - Pasar los bytes a ZXing para intentar decodificar un QR.
 * - Aplicar throttling para no analizar demasiados frames.
 * - Aplicar cooldown para evitar repetir el mismo QR continuamente.
 *
 * Arquitectura:
 * - Capa: infraestructura (módulo `qr/analyzer`)
 * - Rol: procesar frames y detectar QR.
 * - No realiza parseo ni lógica de dominio; solo detecta texto QR.
 *
 * @param onResult Callback que recibe el texto del QR cuando ZXing lo detecta.
 * @param analysisIntervalMs Intervalo mínimo entre análisis consecutivos.
 * @param qrCooldownMs Tiempo mínimo para volver a aceptar el mismo QR.
 */
class QRXZingAnalyzer(
    private val onResult: (String) -> Unit,
    private val analysisIntervalMs: Long = 150L,
    private val qrCooldownMs: Long = 3_000L
) : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "QRXZingAnalyzer"
    }

    // Lector ZXing configurado solo para QR
    private val reader = MultiFormatReader().apply {
        val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.ALSO_INVERTED to true
        )
        setHints(hints)
    }

    private var lastAnalysisTime = 0L
    private val recentQrs = mutableMapOf<String, Long>()
    private var lastCleanupTime = 0L

    // Buffers reutilizables para evitar crear arrays en cada frame
    private var yBytesPool: ByteArray? = null
    private var rowDataPool: ByteArray? = null
    private var rotatedPool: ByteArray? = null

    private var lastWidth = -1
    private var lastHeight = -1

    /**
     * Recibe un frame de CameraX y ejecuta el proceso de detección.
     *
     * Flujo:
     * 1. Throttling: evita analizar frames demasiado seguidos.
     * 2. Copia del plano Y: extrae luminancia del frame.
     * 3. Rotación: ajusta la imagen según orientación.
     * 4. ZXing: intenta decodificar el QR.
     * 5. Cooldown: evita repetir el mismo QR.
     */
    override fun analyze(image: ImageProxy) {
        val now = SystemClock.elapsedRealtime()

        if (now - lastAnalysisTime < analysisIntervalMs) {
            Log.d(TAG, "Frame ignorado por throttling")
            image.close()
            return
        }

        lastAnalysisTime = now

        try {
            Log.d(TAG, "Nuevo frame recibido")

            val yPlane = image.planes[0]
            val width = image.width
            val height = image.height
            val rowStride = yPlane.rowStride

            Log.d(TAG, "Resolución del frame: ${width}x$height")

            ensureBuffers(width, height, rowStride)

            val yBytes = requireNotNull(yBytesPool)
            val rowData = requireNotNull(rowDataPool)
            val yBuffer: ByteBuffer = yPlane.buffer

            yBuffer.rewind()

            // Copia del plano Y
            for (row in 0 until height) {
                yBuffer.position(row * rowStride)
                yBuffer.get(rowData, 0, rowStride)
                System.arraycopy(rowData, 0, yBytes, row * width, width)
            }

            Log.d(TAG, "Plano Y copiado correctamente")

            // Rotación
            val rotation = image.imageInfo.rotationDegrees
            val rotatedBytes = rotateGrayPooled(yBytes, width, height, rotation)

            val rotatedWidth = if (rotation == 90 || rotation == 270) height else width
            val rotatedHeight = if (rotation == 90 || rotation == 270) width else height

            // ZXing
            val source = PlanarYUVLuminanceSource(
                rotatedBytes,
                rotatedWidth,
                rotatedHeight,
                0,
                0,
                rotatedWidth,
                rotatedHeight,
                false
            )

            val bitmap = BinaryBitmap(HybridBinarizer(source))

            Log.d(TAG, "Intentando decodificar QR")

            val result = reader.decode(bitmap)
            val qrText = result.text

            Log.d(TAG, "QR detectado: $qrText")

            // Cooldown
            val lastSeen = recentQrs[qrText] ?: 0L
            val elapsed = now - lastSeen

            if (elapsed > qrCooldownMs) {
                Log.d(TAG, "QR aceptado")
                recentQrs[qrText] = now
                onResult(qrText)
            } else {
                Log.d(TAG, "QR ignorado por cooldown ($elapsed ms)")
            }

            // Limpieza ocasional
            if (now - lastCleanupTime > 5_000L) {
                recentQrs.entries.removeAll { now - it.value > qrCooldownMs * 2 }
                lastCleanupTime = now
                Log.d(TAG, "Mapa recentQrs limpiado")
            }

        } catch (_: NotFoundException) {
            Log.d(TAG, "No se encontró QR en este frame")

        } catch (_: ChecksumException) {
            Log.w(TAG, "QR detectado pero checksum inválido")

        } catch (_: FormatException) {
            Log.w(TAG, "Formato QR inválido")

        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado analizando QR", e)

        } finally {
            reader.reset()
            image.close()
            Log.d(TAG, "Frame cerrado")
        }
    }

    /**
     * Prepara buffers reutilizables para evitar crear arrays en cada frame.
     * Se recrean solo si cambia la resolución o el stride.
     */
    private fun ensureBuffers(width: Int, height: Int, rowStride: Int) {
        if (width != lastWidth || height != lastHeight || yBytesPool == null) {
            yBytesPool = ByteArray(width * height)
            rotatedPool = ByteArray(width * height)
            lastWidth = width
            lastHeight = height
            Log.d(TAG, "Buffers recreados por cambio de resolución")
        }

        if (rowDataPool == null || rowDataPool!!.size < rowStride) {
            rowDataPool = ByteArray(rowStride)
            Log.d(TAG, "rowDataPool recreado")
        }
    }

    /**
     * Rota manualmente la imagen según la orientación del dispositivo.
     * ZXing necesita la imagen correctamente orientada.
     */
    private fun rotateGrayPooled(
        data: ByteArray,
        width: Int,
        height: Int,
        rotation: Int
    ): ByteArray {

        if (rotation == 0) return data

        val output = requireNotNull(rotatedPool)
        var index = 0

        when (rotation) {
            90 -> {
                for (x in 0 until width) {
                    for (y in height - 1 downTo 0) {
                        output[index++] = data[y * width + x]
                    }
                }
            }
            180 -> {
                for (i in data.indices.reversed()) {
                    output[index++] = data[i]
                }
            }
            270 -> {
                for (x in width - 1 downTo 0) {
                    for (y in 0 until height) {
                        output[index++] = data[y * width + x]
                    }
                }
            }
        }

        Log.d(TAG, "Imagen rotada $rotation grados")
        return output
    }
}