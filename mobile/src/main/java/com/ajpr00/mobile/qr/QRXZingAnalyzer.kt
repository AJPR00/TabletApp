package com.ajpr00.mobile.qr

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

class QRXZingAnalyzer(
    private val onResult: (String) -> Unit,

    /*
     * Cada cuánto analizamos un frame.
     *
     * Aunque CameraX mande muchos frames por segundo,
     * no hace falta analizar todos.
     *
     * 150ms = unas 6-7 veces por segundo.
     *
     * Suele ser más que suficiente para un lector QR.
     */
    private val analysisIntervalMs: Long = 150L,

    /*
     * Tiempo de espera para volver a aceptar
     * el mismo QR.
     *
     * Evita spam:
     *
     * hola
     * hola
     * hola
     * hola
     *
     * mientras el usuario deja el QR quieto.
     */
    private val qrCooldownMs: Long = 3_000L
) : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "QRXZingAnalyzer"
    }

    /*
     * ZXing necesita un "reader".
     *
     * Aquí configuramos cómo queremos que lea.
     *
     * POSSIBLE_FORMATS:
     * Solo QR_CODE → así trabaja menos.
     *
     * TRY_HARDER:
     * Se esfuerza más cuando el QR es difícil.
     *
     * ALSO_INVERTED:
     * Permite leer QR blanco sobre negro.
     */
    private val reader = MultiFormatReader().apply {

        val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.ALSO_INVERTED to true
        )

        setHints(hints)
    }

    /*
     * Timestamp del último frame analizado.
     *
     * Lo usamos para el throttling.
     */
    private var lastAnalysisTime = 0L

    /*
     * Aquí guardamos:
     *
     * QR -> momento en el que se leyó
     *
     * Ejemplo:
     *
     * "hola123" -> 12345678
     */
    private val recentQrs = mutableMapOf<String, Long>()

    /*
     * No hace falta limpiar el mapa en cada frame.
     *
     * Lo hacemos cada X segundos.
     */
    private var lastCleanupTime = 0L

    /*
     * Pools reutilizables.
     *
     * Evitamos crear arrays nuevos
     * en cada frame.
     *
     * Esto reduce trabajo del GC
     * (Garbage Collector).
     */
    private var yBytesPool: ByteArray? = null
    private var rowDataPool: ByteArray? = null
    private var rotatedPool: ByteArray? = null

    /*
     * Recordamos tamaño anterior.
     *
     * Si cambia resolución,
     * recreamos buffers.
     */
    private var lastWidth = -1
    private var lastHeight = -1

    override fun analyze(image: ImageProxy) {

        val now = SystemClock.elapsedRealtime()

        /*
         * ===================================
         * PASO 1 → THROTTLING
         * ===================================
         *
         * No analizamos todos los frames.
         *
         * Si han pasado menos de X ms,
         * ignoramos el frame.
         */
        if (now - lastAnalysisTime < analysisIntervalMs) {

            Log.d(TAG,"Frame ignorado por throttling")

            image.close()
            return
        }

        lastAnalysisTime = now

        try {
            Log.d(TAG, "======== NUEVO FRAME ========")

            val yPlane = image.planes[0]

            val width = image.width
            val height = image.height
            val rowStride = yPlane.rowStride

            Log.d(TAG, "Resolución: ${width}x$height")

            /*
             * Creamos/reutilizamos buffers.
             */
            ensureBuffers(width, height, rowStride)

            val yBytes = requireNotNull(yBytesPool)

            val rowData = requireNotNull(rowDataPool)

            val yBuffer: ByteBuffer = yPlane.buffer

            /*
             * Reiniciamos posición buffer.
             */
            yBuffer.rewind()

            /*
             * ===================================
             * PASO 2 → COPIAR PLANO Y
             * ===================================
             *
             * CameraX trabaja con YUV.
             *
             * El plano Y = luminancia
             * (escala de grises).
             *
             * Para QR importa muchísimo
             * más contraste que color.
             *
             * Ojo con rowStride:
             *
             * Android puede meter padding.
             */
            for (row in 0 until height) {

                yBuffer.position(row * rowStride)

                yBuffer.get(rowData, 0, rowStride)

                System.arraycopy(rowData, 0, yBytes, row * width, width)
            }

            Log.d(TAG, "Plano Y preparado")

            /*
             * ===================================
             * PASO 3 → ROTACIÓN
             * ===================================
             *
             * La cámara puede venir girada.
             *
             * ZXing necesita imagen orientada.
             */
            val rotation = image.imageInfo.rotationDegrees

            Log.d(TAG, "Rotación: $rotation")

            val rotatedBytes = rotateGrayPooled(yBytes, width, height, rotation)

            val rotatedWidth = if (rotation == 90 || rotation == 270) height else width

            val rotatedHeight = if (rotation == 90 || rotation == 270) width else height

            /*
             * ===================================
             * PASO 4 → ZXING
             * ===================================
             *
             * Convertimos bytes a un formato
             * que ZXing entienda.
             */
            val source =
                PlanarYUVLuminanceSource(
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

            Log.d(TAG, "Intentando leer QR...")

            /*
             * Intentamos decodificar.
             */
            val result = reader.decode(bitmap)

            val qrText = result.text

            Log.d(TAG, "QR detectado: $qrText")

            /*
             * ===================================
             * PASO 5 → COOLDOWN
             * ===================================
             *
             * Evitamos disparar el mismo QR
             * constantemente.
             */
            val lastSeen = recentQrs[qrText] ?: 0L

            val elapsed = now - lastSeen

            if (elapsed > qrCooldownMs) {
                Log.d(TAG, "QR aceptado")

                recentQrs[qrText] = now
                onResult(qrText)

            } else {
                Log.d(TAG, "QR ignorado por cooldown (${elapsed}ms)")
            }

            /*
             * Limpieza ocasional.
             *
             * No hace falta hacerla
             * en todos los frames.
             */
            if (now - lastCleanupTime > 5_000L) {

                recentQrs.entries.removeAll {
                    now - it.value > qrCooldownMs * 2
                }

                lastCleanupTime = now

                Log.d(TAG, "Mapa recentQrs limpiado")
            }

        } catch (_: NotFoundException) {

            /*
             * Caso NORMAL.
             *
             * Simplemente:
             * no había QR.
             */
            Log.d(TAG, "No se encontró QR")

        } catch (_: ChecksumException) {

            Log.w(TAG, "QR detectado pero checksum inválido")

        } catch (_: FormatException) {

            Log.w(TAG, "Formato QR inválido")

        } catch (e: Exception) {

            /*
             * Algo raro pasó.
             */
            Log.e(TAG, "Error inesperado", e)

        } finally {

            /*
             * MUY IMPORTANTE.
             *
             * Si no cierras el frame,
             * CameraX se atasca.
             */
            reader.reset()

            image.close()

            Log.d(TAG, "Frame cerrado")
        }
    }

    /*
     * Crea/reutiliza buffers.
     *
     * Solo recreamos si cambia
     * resolución.
     */
    private fun ensureBuffers(
        width: Int,
        height: Int,
        rowStride: Int
    ) {

        if (width != lastWidth || height != lastHeight || yBytesPool == null) {

            yBytesPool = ByteArray(width * height)

            rotatedPool = ByteArray(width * height)

            lastWidth = width
            lastHeight = height

            Log.d(TAG, "Buffers recreados")
        }

        if (rowDataPool == null || rowDataPool!!.size < rowStride) {

            rowDataPool = ByteArray(rowStride)

            Log.d(TAG, "rowDataPool recreado")
        }
    }

    /*
     * Rotación manual imagen.
     *
     * 90°
     * 180°
     * 270°
     *
     * Esto es manipulación de índices.
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