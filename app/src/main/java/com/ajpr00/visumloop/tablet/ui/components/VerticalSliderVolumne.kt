package com.ajpr00.visumloop.tablet.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color

@Composable
fun VerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trackColor: Color = MaterialTheme.colorScheme.primary,
    inactiveTrackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
) {
    Box(
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .rotate(-90f) // rotate counter-clockwise to make it vertical
        ) {
            Slider(
                value = value.coerceIn(0f, 1f),
                onValueChange = { onValueChange(it.coerceIn(0f, 1f)) },
                enabled = enabled,
                valueRange = 0f..1f,
                steps = 0,
                colors = SliderDefaults.colors(
                    thumbColor = trackColor,
                    activeTrackColor = trackColor,
                    inactiveTrackColor = inactiveTrackColor
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
