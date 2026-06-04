package com.ajpr00.components.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ajpr00.core.domain.model.MediaContent

// --------------------------------------------------------------
// TARJETA INDIVIDUAL DE LA GRID
// --------------------------------------------------------------
// Cada tarjeta muestra:
// - Miniatura (imagen o vídeo)
// - Botón de favorito
// --------------------------------------------------------------
@Composable
fun MediaCardGrid(
    media: MediaContent,
    onClick: () -> Unit,
    onToggleAccion: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() } // pulsar la tarjeta
    ) {
        // Miniatura del archivo (imagen o vídeo)
       MediaThumbnail(
            path = media.path,
            type = media.type,
            modifier = Modifier.fillMaxSize()
        )

        // Botón de favorito en la esquina superior derecha
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = onToggleAccion
        ) {
           /* Icon(
                imageVector = if (media.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (media.isFavorite) "Favorito" else "No favorito",
                tint = if (media.isFavorite) Color.Red else Color.Gray
            )*/
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Favorito",
                tint = Color.Red
            )
        }
    }
}

