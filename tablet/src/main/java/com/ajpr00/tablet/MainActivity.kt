package com.ajpr00.tablet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.ajpr00.components.theme.VisumLoopAppTheme
import com.ajpr00.presentation_common.auth.FacebookInitializer
import com.ajpr00.presentation_common.viewmodel.AuthViewModel
import com.ajpr00.tablet.presentation.viewmodel.SettingsViewModel
import com.ajpr00.tablet.ui.navigation.NavigationCore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    @Inject lateinit var facebookInitializer: FacebookInitializer


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
                language = settingsState.value.language
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavigationCore(
                        innerPadding = innerPadding,
                        authViewModel = authViewModel,
                        settingsViewModel = settingsViewModel,
                        onFacebookLogin = {
                            Log.d("MainActivity", "Iniciando login con Facebook desde Compose")
                            facebookInitializer.startLogin(this)
                        }                    )
                }
            }
        }

        lifecycleScope.launch {
            Log.d("MainActivity", "Inicializando FacebookInitializer")
            facebookInitializer.init(authViewModel)
        }
    }

    /**
     * Recibe el resultado del login de Facebook.
     *
     * Este método es obligatorio porque el SDK de Facebook **no soporta**
     * Activity Result API. Por eso se mantiene `onActivityResult`.
     *
     * @param requestCode Código de solicitud.
     * @param resultCode Código de resultado.
     * @param data Intent con los datos devueltos por Facebook.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("MainActivity", "Delegando resultado de Facebook Login al FacebookInitializer")
        super.onActivityResult(requestCode, resultCode, data)
        facebookInitializer.onActivityResult(requestCode, resultCode, data)
    }
}