package com.ajpr00.mobile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ajpr00.core.domain.model.EstadoDispositivo
import com.ajpr00.mobile.ui.model.DispositivoUi
import com.ajpr00.visumloop.mobile.R

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DispositivoCardPreview() {
    val dispositivo = DispositivoUi(
        nombre = "Tablet 1",
        nivelBatery = 80,
        icono = R.drawable.ic_tablet,
        estado = EstadoDispositivo.ONLINE,
        id = "1"
    )
    DispositivoCard(
        size = 300.dp,
        modifier = Modifier,
        dispositivo = dispositivo)
}

@Composable
fun DispositivoCard(
    size: Dp,
    modifier: Modifier = Modifier,
    dispositivo: DispositivoUi,
    onClick: () -> Unit = {}
) {
    val titleSize = (size * 0.038f).value.sp
    val statusSize = (size * 0.03f).value.sp
    val batterySize = (size * 0.03f).value.sp
    val iconSize = (size * 0.15f)
    val iconBattery = (size * 0.04f)

    Card(
        modifier = modifier
            .width(size/2.5f)
            .aspectRatio(1.6f)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {

            dispositivo.nivelBatery?.let { nivel ->
                Row (modifier = Modifier.align(Alignment.TopEnd)) {
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
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp),
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
