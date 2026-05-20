package com.ajpr00.mobile.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.ajpr00.mobile.presentation.viewmodel.PanelControlViewModel
import com.ajpr00.mobile.ui.components.DrawerMenu
import com.ajpr00.mobile.ui.components.FloatingBottomBar
import com.ajpr00.mobile.ui.components.TopBar
import com.ajpr00.mobile.ui.screen.LoginScreenMobile
import com.ajpr00.mobile.ui.screen.RecoverPasswordMobile
import com.ajpr00.mobile.ui.screen.RegisterMobile
import com.ajpr00.mobile.ui.screen.ScreenPanelControl
import com.ajpr00.mobile.ui.screen.SplashScreenMobile
import com.ajpr00.presentation_common.viewmodel.LoginViewModel
import com.ajpr00.presentation_common.viewmodel.RegisterViewModel
import com.ajpr00.presentation_common.viewmodel.SplashViewModel
import kotlinx.coroutines.launch

@Composable
fun NavigationCore() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val hideBars = isBlackBar(currentRoute)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf("inicio") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp)
            ) {
                DrawerMenu(
                    selectedItem = selectedItem,
                    onItemSelected = { item ->
                        Log.d("DRAWER", "➡️ Usuario seleccionó: $item")
                        selectedItem = item
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,

            topBar = {
                if (!hideBars) {
                    Log.d("UI", "🧢 Mostrando TopBar (estamos en MainGraph)")
                    TopBar(
                        onMenuClick = {
                            Log.d("DRAWER", "📂 Abriendo menú lateral")
                            scope.launch { drawerState.open() }
                        }
                    )
                } else {
                    Log.d("UI", "🧢 TopBar oculto (no estamos en MainGraph)")
                }
            },

            bottomBar = {
                if (!hideBars) {
                    Log.d("UI", "🦶 Mostrando BottomBar (MainGraph activo)")
                    FloatingBottomBar()
                }
            }
        ) { innerPadding ->

            Log.d("LAYOUT", "📐 innerPadding aplicado: $innerPadding")

            NavHost(
                modifier = Modifier.padding(innerPadding),
                navController = navController,
                startDestination = MainGraph
            ) {

                // 🚀 SPLASH
                composable<Splash> { backStackEntry ->
                    Log.d("NAV", "🚀 Entrando en SplashScreen")

                    val viewModelSplash: SplashViewModel = hiltViewModel(backStackEntry)

                    SplashScreenMobile(
                        viewModel = viewModelSplash,
                        onNavigateToMain = {
                            Log.d("NAV", "➡️ Splash → MainGraph")
                            navController.navigate(MainGraph)
                        },
                        onNavigateToLogin = {
                            Log.d("NAV", "➡️ Splash → LoginGraph")
                            navController.navigate(LoginGraph)
                        }
                    )
                }

                // 🔐 LOGIN GRAPH
                navigation<LoginGraph>(startDestination = LoginScreen) {

                    composable<LoginScreen> {
                        Log.d("NAV", "🔐 Entrando en LoginScreen")

                        val viewModelLoginViewModel: LoginViewModel = hiltViewModel()

                        LoginScreenMobile(
                            viewModel = viewModelLoginViewModel,
                            goToMainGraph = {
                                Log.d("NAV", "🔐 Login correcto → MainGraph")
                                navController.navigate(MainGraph)
                            },
                            goToFromRegistro = {
                                Log.d("NAV", "📝 Login → RegisterScreen")
                                navController.navigate(RegisterScreen)
                            },
                            goToRecuperarPass = {
                                Log.d("NAV", "🔑 Login → RecoverPassword")
                                navController.navigate(FromRecover)
                            },
                        )
                    }
                }

                // 📝 REGISTER
                composable<RegisterScreen> {
                    Log.d("NAV", "📝 Entrando en RegisterScreen")

                    val viewModelLoginViewModel: RegisterViewModel = hiltViewModel()
                    RegisterMobile(
                        viewModel = viewModelLoginViewModel,
                        goToBack = {
                            Log.d("NAV", "⬅️ Register → Back")
                            navController.popBackStack()
                        }
                    )
                }

                // 🔑 RECOVER PASSWORD
                composable<FromRecover> {
                    Log.d("NAV", "🔑 Entrando en RecoverPasswordScreen")

                    val viewModelLoginViewModel: RegisterViewModel = hiltViewModel()
                    RecoverPasswordMobile(
                        viewModel = viewModelLoginViewModel,
                        goToBack = {
                            Log.d("NAV", "⬅️ RecoverPassword → Back")
                            navController.popBackStack()
                        }
                    )
                }

                // 🏠 MAIN GRAPH
                navigation<MainGraph>(startDestination = PanelControl) {

                    composable<PanelControl> { backStackEntry ->
                        Log.d("NAV", "🏠 Entrando en PanelControl (MainGraph)")

                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry(MainGraph::class.qualifiedName!!)
                        }

                        val viewModelPanelControl: PanelControlViewModel =
                            hiltViewModel(parentEntry)

                        ScreenPanelControl(
                            modifier = Modifier,
                            viewModel = viewModelPanelControl,
                            onSelectImage = {
                                Log.d("NAV", "⬆️ PanelControl → SelectImage")
                                // navController.navigate(SelectImage)
                            },
                            onSelectVideo = {
                                Log.d("NAV", "⬆️ PanelControl → SelectVideo")
                                //  navController.navigate(SelectVideo)
                            },
                            onEnviar = {
                                Log.d("NAV", "⬆️ PanelControl → Enviar")
                                // navController.navigate(Enviar)
                            },
                            onVerArchivos = {
                                Log.d("NAV", "⬆️ PanelControl → VerArchivos")
                                // navController.navigate(VerArchivos)
                            },
                            onConfiguracion = {
                                Log.d("NAV", "⬆️ PanelControl → Configuracion")
                            },
                            onAgregarDispositivo = {
                                Log.d("NAV", "⬆️ PanelControl → AgregarDispositivo")
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun isBlackBar(currentRoute: String?): Boolean {
    if (currentRoute == null) return false

    val listBlackBar = listOf(
        Splash::class.simpleName!!,
        LoginScreen::class.simpleName!!,
        RegisterScreen::class.simpleName!!,
        FromRecover::class.simpleName!!
    )

    return listBlackBar.any { currentRoute.contains(it) }
}
