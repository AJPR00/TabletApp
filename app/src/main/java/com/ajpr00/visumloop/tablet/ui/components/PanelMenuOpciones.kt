package com.ajpr00.visumloop.tablet.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun ButtonCustonPanel(
    modifier: Modifier = Modifier.padding(5.dp),
    shape: Shape = RoundedCornerShape(50),
    size: Dp,
    icon: ImageVector,
    iconSize: Dp = 90.dp,
    label: String,
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.size(size),
        shape = shape,
        colors = ButtonDefaults.textButtonColors(
            containerColor = color,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(iconSize) // 👈 aquí aplicas el tamaño
            )
            Text(text = label, textAlign = TextAlign.Center)
        }
    }
}