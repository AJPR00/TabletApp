package com.ajpr00.tablet.presentation.qr

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.core.graphics.set
import androidx.core.graphics.createBitmap

/**
 * Genera un QR como ImageBitmap a partir de un String.
 *
 * @param data Contenido del QR (normalmente JSON).
 * @param size Tamaño del QR en píxeles.
 */
fun generateQrBitmap(data: String, size: Int = 512): Bitmap {
    val bitMatrix = QRCodeWriter().encode(
        data,
        BarcodeFormat.QR_CODE,
        size,
        size
    )

    val width = bitMatrix.width
    val height = bitMatrix.height
    val bmp = createBitmap(width, height, Bitmap.Config.RGB_565)

    for (x in 0 until width) {
        for (y in 0 until height) {
            bmp[x, y] = if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        }
    }

    return bmp
}
