package com.ajpr00.components.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import com.ajpr00.core.domain.model.FormatType

// --------------------------------------------------------------
// 🖼️ MINIATURA DE IMAGEN / 🎬 MINIATURA DE VÍDEO
// --------------------------------------------------------------
// Esta función detecta si el archivo es imagen, vídeo o audio.
// - Si es imagen → Coil la carga directamente.
// - Si es vídeo → generamos una miniatura con MediaMetadataRetriever.
// --------------------------------------------------------------
@Composable
fun MediaThumbnail(
    path: String,
    type: FormatType,
    modifier: Modifier = Modifier
) {
    when (type) {

        FormatType.IMAGE -> {
            Image(
                painter = rememberAsyncImagePainter(path),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
        // Aquí viene la magia: generamos una miniatura del vídeo.
        // remember(path) asegura que SOLO se calcule una vez por vídeo.
        // --------------------------------------------------------------
        FormatType.VIDEO -> {
            val context = LocalContext.current

            val bitmap = remember(path) { getVideoThumbnail(path, context) }

            bitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = modifier
                )
            } ?: Box(
                modifier = modifier.background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                // Si no hay miniatura, mostramos un icono de "play"
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
        FormatType.UNKNOWN -> {
            Box(
                modifier = modifier.background(Color(0xFF333333)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DeviceUnknown,
                    contentDescription = "Audio",
                    tint = Color.White
                )
            }
        }
    }
}