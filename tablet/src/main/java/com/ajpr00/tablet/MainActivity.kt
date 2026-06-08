package com.ajpr00.tablet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.ajpr00.components.theme.VisumLoopAppTheme
import com.ajpr00.tablet.presentation.viewmodel.SettingsViewModel
import com.ajpr00.tablet.ui.navigation.NavigationCore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Evita que la pantalla se apague
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        enableEdgeToEdge()
        setContent {
            val settingsState = settingsViewModel.state.collectAsState()

            VisumLoopAppTheme(
                darkTheme = settingsState.value.isDarkMode,
                dynamicColor = false,
                language = settingsState.value.language   // ← AÑADIDO
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavigationCore(innerPadding, settingsViewModel)
                }
            }
        }
    }
}