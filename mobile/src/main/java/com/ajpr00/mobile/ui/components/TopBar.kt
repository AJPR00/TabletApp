package com.ajpr00.mobile.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text("Panel VisumControl") },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = { /* Search */ }) {
                Icon(Icons.Default.SupervisedUserCircle, contentDescription = "Search")
            }
        }
    )
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Preview TopBar"
)
@Composable
fun TopBarPreview() {
    TopBar(
        onMenuClick = {
            // [Logica_preview_menu_click](ca://s?q=Logica_preview_menu_click)
        }
    )
}

