package com.ajpr00.components.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ajpr00.core.domain.model.MediaContent

@Composable
fun MediaCard(
    media: MediaContent,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Box(modifier = Modifier.clickable { onClick() }) {
        MediaThumbnail(
            path = media.path,
            type = media.type,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(10.dp))
        )

        IconButton(
            modifier = Modifier.align(alignment = Alignment.BottomEnd),
            onClick = onToggleFavorite
        ) {
            /*Icon(
                imageVector = if (media.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (media.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
            )*/
        }
    }
}