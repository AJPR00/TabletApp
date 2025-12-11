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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ajpr00.visumloop.tablet.domain.model.FormatType
import com.ajpr00.visumloop.tablet.domain.model.MediaContent
import com.ajpr00.visumloop.tablet.presentation.viewmodel.ReproducorViewModel
import com.ajpr00.visumloop.tablet.presentation.viewmodel.MediaItemsViewModel

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
    viewModelMediaItems: MediaItemsViewModel,
    viewModelMediaBackground: ReproducorViewModel,
    label: String = "",
) {
    val items by viewModelMediaItems.mediaItems.collectAsState()
    val lis by viewModelMediaBackground.mediaList.collectAsState()

    LaunchedEffect(items) {
        Log.d("MediaList", "MediaList items actualizados: ${items.size}")
    }

    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .border(
                // color = MaterialTheme.colorScheme.onPrimary,
                color = Color.Black,
                width = 5.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            ),
    ) {
        Text(label, modifier = Modifier.align(Alignment.TopCenter))
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            columns = GridCells.Adaptive(minSize = 150.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(lis) { item ->
                Log.d(
                    "MediaList",
                    "Mostrando item: ${item.name}, type: ${item.type}, path: ${item.path}"
                )
                MediaCardGrid(
                    media = item,
                    onClick = {
                        Log.d("MediaList", "Click en item: ${item.name}")
                    },
                    onToggleFavorite = {
                        Log.d("MediaList", "Toggle favorite en item: ${item.name}")
                        viewModelMediaItems.toggleFavorite(item)
                    }
                )
            }
        }
    }
}

@Composable
fun MediaCardGrid(
    media: MediaContent,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
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
                contentDescription = if (media.isFavorite) "Favorito" else "No favorito",
                tint = if (media.isFavorite) Color.Red else Color.Gray
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