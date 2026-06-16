package com.ajpr00.mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.ajpr00.components.theme.VisumLoopAppTheme
import com.ajpr00.mobile.presentation.viewmodel.MobileConnectionViewModel
import com.ajpr00.mobile.presentation.viewmodel.SettingsViewModel
import com.ajpr00.mobile.ui.navigation.NavigationCore
import com.ajpr00.presentation_common.auth.FacebookInitializer
import com.ajpr00.presentation_common.viewmodel.AuthViewModel
import com.ajpr00.presentation_common.viewmodel.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue

/**
 * Activity principal del módulo **mobile**.
 *
 * Esta clase actúa como punto de entrada de la aplicación en dispositivos móviles.
 * Su responsabilidad es estrictamente de **UI + orquestación**, sin contener lógica
 * de dominio ni lógica de infraestructura.
 *
 * ## Responsabilidades principales
 * - Inicializar los ViewModels de configuración, autenticación y conectividad.
 * - Gestionar el SplashScreen mediante [SplashViewModel](ca://s?q=Documentar_SplashViewModel).
 * - Iniciar el flujo de login con Facebook a través de [FacebookInitializer](ca://s?q=Documentar_FacebookInitializer).
 * - Montar la UI de Compose mediante [NavigationCore](ca://s?q=Documentar_NavigationCore).
 * - Lanzar la monitorización de red mediante [MobileConnectionViewModel](ca://s?q=Documentar_MobileConnectionViewModel).
 *
 * ## Relación con la arquitectura multimódulo
 * - **mobile** → capa de presentación específica del dispositivo.
 * - **presentation_common** → lógica compartida entre mobile/tablet (Auth, Facebook, Splash).
 * - **core** → casos de uso y repositorios (no se tocan aquí).
 *
 * ## Flujo interno del login con Facebook
 * 1. `onCreate()` inicializa el SplashScreen.
 * 2. Se registra el callback del SDK mediante `facebookInitializer.init()`.
 * 3. El usuario pulsa el botón de login en Compose.
 * 4. `NavigationCore` llama a `onFacebookLogin`.
 * 5. `facebookInitializer.startLogin()` abre la UI de Facebook.
 * 6. El resultado vuelve a `onActivityResult`.
 * 7. `facebookInitializer.onActivityResult()` procesa el resultado.
 * 8. El token se envía al `AuthViewModel`.
 *
 * ## Flujo interno de conectividad
 * 1. `onCreate()` lanza `mobileConnectionViewModel.startNetworkMonitoring()`.
 * 2. El ViewModel observa cambios de red (BroadcastReceiver o NetworkCallback según API).
 * 3. La UI puede reaccionar a cambios de conexión.
 *
 * ## Advertencias importantes
 * - Facebook Login **requiere un Activity real**, no funciona desde ViewModel.
 * - `onActivityResult` es obligatorio porque el SDK no soporta Activity Result API.
 * - Esta Activity no debe contener lógica de negocio.
 * - La monitorización de red debe iniciarse desde un contexto con ciclo de vida.
 *
 * ## Excepciones
 * - Puede lanzar `VisumException` si el inicializador de Facebook falla.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val mobileConnectionViewModel: MobileConnectionViewModel by viewModels()
    private val splashViewModel: SplashViewModel by viewModels()

    @Inject lateinit var facebookInitializer: FacebookInitializer

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d("MainActivity", "Inicializando SplashScreen")

        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "Configurando contenido Compose")
        setContent {
            val settingsState = settingsViewModel.state.collectAsState()

            // Notificación al ViewModel de que la app ha arrancado
            LaunchedEffect(Unit) {
                Log.d("MainActivity", "Notificando arranque de la app a MobileConnectionViewModel")
                mobileConnectionViewModel.onAppStarted()
            }

            VisumLoopAppTheme(
                darkTheme = settingsState.value.isDarkMode,
                dynamicColor = false,
                language = settingsState.value.language
            ) {
                NavigationCore(
                    viewModelAuth = authViewModel,
                    viewModelSettings = settingsViewModel,
                    onFacebookLogin = {
                        Log.d("MainActivity", "Iniciando login con Facebook desde Compose")
                        facebookInitializer.startLogin(this)
                    }
                )
            }
        }

        lifecycleScope.launch {
            Log.d("MainActivity", "Iniciando monitorización de red")
            mobileConnectionViewModel.startNetworkMonitoring()

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