package com.ajpr00.visumloop.tablet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun CustomButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icono: Int,
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier
            .width(250.dp)
            .height(50.dp),
        enabled = enabled,
        onClick = onClick,
        border = BorderStroke(1.dp, Color.Black),
        shape = CircleShape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,   // fondo neutro
        )
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified,
            painter = painterResource(icono),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

