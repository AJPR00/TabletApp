package com.ajpr00.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ajpr00.core.domain.model.FormatType
import com.ajpr00.core.domain.model.PendingMedia
import com.ajpr00.uicommon.R

@Composable
fun MediaPreviewCard(
    media: PendingMedia,
    size: Dp = 100.dp
) {
    Card(
        modifier = Modifier
            .width(size)
            .aspectRatio(1f)
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        when (media.type) {

            FormatType.IMAGE -> {
                AsyncImage(
                    model = media.filePath,
                    contentDescription = "Imagen seleccionada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            FormatType.VIDEO -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.google_logo),
                        contentDescription = "Miniatura de video",
                        tint = Color.White,
                        modifier = Modifier.size(size * 0.4f)
                    )
                }
            }

            FormatType.UNKNOWN -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("?", color = Color.White)
                }
            }
        }
    }
}
