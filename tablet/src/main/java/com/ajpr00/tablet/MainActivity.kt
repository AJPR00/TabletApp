package com.ajpr00.tablet

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.ajpr00.components.theme.VisumLoopAppTheme
import com.ajpr00.tablet.data.server.ServerService   // ← IMPORTANTE
import com.ajpr00.tablet.ui.navigation.NavigationCore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Evita que la pantalla se apague
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Inicia el servidor en un ForegroundService
        upService()

        enableEdgeToEdge()
        setContent {
            VisumLoopAppTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavigationCore(innerPadding)
                }
            }
        }
    }
}

private fun MainActivity.upService() {
    val intent = Intent(this, ServerService::class.java)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
    } else {
        startService(intent)
    }
}
