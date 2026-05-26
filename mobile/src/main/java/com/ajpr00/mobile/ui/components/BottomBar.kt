package com.ajpr00.mobile.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

@Preview(
    showBackground = true, showSystemUi = true,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL, backgroundColor = 0xFF780000
)
@Composable
fun PreviewBottom() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            FloatingBottomBar()
        }
    }
}
@Composable
fun FloatingBottomBar(
    deleteDispositivos: () -> Unit = {},
    configuracion: () -> Unit = {}
) {
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = navBarPadding.calculateBottomPadding())
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        val barWidth = maxWidth * 0.45f
        val barHeight = maxWidth * 0.1f
        val shape = MaterialTheme.shapes.extraLarge

        Row(
            modifier = Modifier
                .width(barWidth)
                .height(barHeight)
                .clip(shape)
                .background(Color.Yellow.copy(alpha = 0.20f))
        ) {
            IconButton(
                onClick = configuracion,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Configuración")
            }

            IconButton(
                onClick = deleteDispositivos,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}