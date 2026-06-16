package com.ajpr00.components.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchingManualIndicator() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(42.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Buscando dispositivo...",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
