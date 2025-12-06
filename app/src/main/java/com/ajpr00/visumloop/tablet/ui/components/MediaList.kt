package com.ajpr00.visumloop.tablet.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ajpr00.visumloop.tablet.domain.model.FormatType
import com.ajpr00.visumloop.tablet.domain.model.MediaContent

/*@Composable
fun MediaList(
    items: List<MediaContent>,
    onItemClick: (MediaContent) -> Unit,
    onToggleFavorite: (MediaContent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            MediaCard(
                media = item,
                onClick = { onItemClick(item) },
                onToggleFavorite = { onToggleFavorite(item) }
            )
        }
    }
}*/
@Composable
fun MediaList(
    label: String="",
    items: List<MediaContent>,
    onItemClick: (MediaContent) -> Unit,
    onToggleFavorite: (MediaContent) -> Unit
) {
    Log.d("Gestos", "Estoy en MediaList")

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
            .border(
                color = MaterialTheme.colorScheme.onPrimary,
                width = 5.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(label)
            Spacer(modifier = Modifier.size(16.dp))
        }
        items(items) { item ->
            MediaCardGrid(
                media = item,
                onClick = { onItemClick(item) },
                onToggleFavorite = { onToggleFavorite(item) }
            )
        }
    }
}

@Composable
fun MediaCardGrid(
    media: MediaContent,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        MediaThumbnail(
            path = media.path,
            type = media.type,
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = onToggleFavorite
        ) {
            Icon(
                imageVector = if (media.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite"
            )
        }
    }
}

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
            Icon(
                imageVector = if (media.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (media.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

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
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        FormatType.AUDIO -> {
            Box(
                modifier = modifier.background(Color(0xFF333333)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Audio",
                    tint = Color.White
                )
            }
        }
    }
}

fun getVideoThumbnail(path: String, context: Context): Bitmap? {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val bitmap = retriever.getFrameAtTime(0)
        retriever.release()
        bitmap
    } catch (e: Exception) {
        null
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMediaList() {
    val items = listOf(
        MediaContent(
            id = 1,
            name = "Imagen de muestra",
            path = "https://picsum.photos/200",
            type = FormatType.IMAGE,
            isFavorite = false
        ),
        MediaContent(
            id = 2,
            name = "Video de muestra",
            path = "sample_video.mp4",
            type = FormatType.VIDEO,
            isFavorite = true
        ),
        MediaContent(
            id = 3,
            name = "Audio de prueba",
            path = "sample_audio.mp3",
            type = FormatType.IMAGE,
            isFavorite = false
        )
    )

    MediaList(
        label = "Mis Archivos",
        items = items,
        onItemClick = {},
        onToggleFavorite = {}
    )
}
