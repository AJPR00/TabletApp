package com.ajpr00.components.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ajpr00.uicommon.R

@Composable
fun ButtonCustonPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12),
    icon: Painter,
    iconSize: Dp = 64.dp,
    label: String,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    ElevatedButton(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f), shape = shape,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = color,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(top = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            val esVector = icon is VectorPainter

            if (esVector) {
                Icon(
                    painter = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(iconSize)
                )
            } else {
                Image(
                    painter = icon,
                    contentDescription = label,
                    modifier = Modifier
                        .size(iconSize)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                textAlign = TextAlign.Center,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
