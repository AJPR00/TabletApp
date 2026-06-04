package com.ajpr00.mobile.ui.components

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ajpr00.core.domain.model.EstadoDispositivo
import com.ajpr00.core.domain.model.FormatType
import com.ajpr00.core.domain.model.RemoteMedia
import com.ajpr00.mobile.ui.model.DispositivoUi
import com.ajpr00.visumloop.mobile.R

/**
 * Card para los Dispositivos almacenado bd, vinculados,
 */
@Composable
fun DispositivoCard(
    modifier: Modifier = Modifier,
    size: Dp,
    dispositivo: DispositivoUi,
    isSelected: Boolean,
    onClick: () -> Unit = {}
) {
    //coerceAtLeast no baja mas de lo permitido por accesibilidad.
    val titleSize = (size * 0.08f).coerceAtLeast(12.dp).value.sp
    val statusSize = (size * 0.1f).coerceAtLeast(12.dp).value.sp
    val batterySize = (size * 0.1f).coerceAtLeast(12.dp).value.sp

    val iconSize = (size * 0.4f)
    val iconBattery = (size * 0.08f)

    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val elevation = if (isSelected) 12.dp else 4.dp

    Card(
        modifier = modifier
            .width(size)
            .aspectRatio(1.6f)
            .clickable { onClick() },
        border = BorderStroke(2.dp, borderColor),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(elevation),
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
        ) {

            dispositivo.nivelBatery?.let { nivel ->
                Row(modifier = Modifier.align(Alignment.TopEnd)) {
                    Text(
                        modifier = Modifier,
                        text = "$nivel%",
                        fontSize = batterySize,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Icon(
                        modifier = Modifier.size(iconBattery),
                        painter = painterResource(id = R.drawable.ic_battery_full_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Text(
                modifier = Modifier.align(Alignment.BottomEnd),
                fontSize = statusSize,
                text = dispositivo.estado.name.lowercase()
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = when (dispositivo.estado) {
                    EstadoDispositivo.ONLINE -> Color(0xFF4CAF50)
                    EstadoDispositivo.OFFLINE -> Color(0xFFF44336)
                    EstadoDispositivo.DESCONOCIDO -> Color.Gray
                }
            )
            Text(
                modifier = Modifier
                    .align(Alignment.TopStart),
                fontSize = titleSize,
                text = dispositivo.nombre,
                style = MaterialTheme.typography.bodyLarge
            )

            Icon(
                modifier = Modifier
                    .size(iconSize)
                    .align(Alignment.BottomStart),
                painter = painterResource(id = dispositivo.icono),
                contentDescription = null,
                tint = Color.Unspecified
            )

        }
    }
}

/**
 * Card para mostrar un dispositivo encontrado.
 */
@Composable
fun DiscoveredDeviceCard(
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    isSelected: Boolean,
    dispositivo: DispositivoUi,
    onClick: () -> Unit = {}
) {
    // 🔥 LOG 5: Qué recibe la Card
    Log.d(
        "DiscoveredDeviceCard",
        "Render → nombre=${dispositivo.nombre}, id=${dispositivo.id}, isSelected=$isSelected"
    )

    val titleSize = (size * 0.05f).coerceAtLeast(14.dp).value.sp
    val cardSize = (size).coerceAtLeast(48.dp)
    val iconSize = (size * 0.15f)

    Card(
        modifier = modifier
            .padding(bottom = 12.dp)
            .width(cardSize)
            .aspectRatio(3.7f)
            .clickable {
                Log.d("DiscoveredDeviceCard", "CLICK en card → ${dispositivo.nombre}")
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painterResource(id = dispositivo.icono),
                contentDescription = null,
                tint = Color.Unspecified
            )

            Spacer(Modifier.width(12.dp))

            Text(
                modifier = Modifier.weight(1f),
                fontSize = titleSize,
                text = dispositivo.nombre,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RemoteMediaCard(
    media: RemoteMedia,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val imageBitmap = remember(media.thumbnailBytes) {
        try {
            BitmapFactory.decodeByteArray(
                media.thumbnailBytes,
                0,
                media.thumbnailBytes.size
            )?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = modifier
            .size(150.dp)
            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(8.dp)
    ) {

        // Imagen o placeholder
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = media.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        when (media.type) {
                            FormatType.IMAGE -> R.drawable.ic_table_disabled
                            FormatType.VIDEO -> R.drawable.ic_tablet
                            else -> R.drawable.ic_tablet_nknown
                        }
                    ),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Nombre abajo
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            Text(
                text = media.name,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
    }
}
