package com.ajpr00.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ajpr00.mobile.ui.navigation.NavigationCore
import com.ajpr00.mobile.ui.theme.TabletAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TabletAppTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                NavigationCore()
            }
        }
    }
}
