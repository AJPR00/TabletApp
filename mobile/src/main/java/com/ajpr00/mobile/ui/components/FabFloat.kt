package com.ajpr00.mobile.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun FabFloat(
    modifier: Modifier = Modifier,
    icon: Painter,
    icDesc: String? = null,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = FloatingActionButtonDefaults.smallShape,
        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(modifier = Modifier.fillMaxSize().padding(8.dp), painter = icon, contentDescription = icDesc)
    }
}
