package com.ajpr00.components.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingButtonContent(
    text: String,
    modifier: Modifier = Modifier
) {
    Row {
        CircularProgressIndicator(
            modifier = modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = LocalContentColor.current
        )
        Spacer(Modifier.width(12.dp))
        Text(text)
    }
}

@Composable
fun DynamicButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isEnabled: Boolean,
    text: String,
    loadingText: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier
    ) {
        if (isLoading) {
            LoadingButtonContent(loadingText)
        } else {
            Text(text)
        }
    }
}
