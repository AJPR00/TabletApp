package com.ajpr00.tabletapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.ajpr00.tabletapp.ui.screen.ReproductorScreen
import com.ajpr00.tabletapp.ui.theme.TabletAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TabletAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ReproductorScreen(
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}