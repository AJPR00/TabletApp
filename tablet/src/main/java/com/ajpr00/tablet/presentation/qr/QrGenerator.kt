package com.ajpr00.tablet.presentation.qr

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

/**
 * Composable que muestra un QR generado a partir de un String.
 *
 * @param data Contenido del QR.
 * @param modifier Modificador para tamaño, padding, etc.
 */
@Composable
fun QrGenerator(
    data: String,
    modifier: Modifier = Modifier
) {
    val bitmap = generateQrBitmap(data)
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "QR Code",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}