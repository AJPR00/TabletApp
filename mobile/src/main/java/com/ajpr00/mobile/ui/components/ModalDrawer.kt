package com.ajpr00.mobile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DrawerMenu(
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            "Menú",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        NavigationDrawerItem(
            label = { Text("Inicio") },
            selected = selectedItem == "inicio",
            onClick = { onItemSelected("inicio") },
            icon = { Icon(Icons.Default.Home, contentDescription = null) }
        )

        NavigationDrawerItem(
            label = { Text("Perfil") },
            selected = selectedItem == "perfil",
            onClick = { onItemSelected("perfil") },
            icon = { Icon(Icons.Default.Person, contentDescription = null) }
        )

        NavigationDrawerItem(
            label = { Text("Ajustes") },
            selected = selectedItem == "ajustes",
            onClick = { onItemSelected("ajustes") },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) }
        )

        NavigationDrawerItem(
            label = { Text("Ayuda") },
            selected = selectedItem == "ayuda",
            onClick = { onItemSelected("ayuda") },
            icon = { Icon(Icons.Default.Info, contentDescription = null) }
        )
    }
}

