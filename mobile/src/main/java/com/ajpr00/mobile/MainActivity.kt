package com.ajpr00.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ajpr00.components.theme.VisumLoopAppTheme
import com.ajpr00.mobile.ui.navigation.NavigationCore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VisumLoopAppTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                NavigationCore()
            }
        }
    }
}
