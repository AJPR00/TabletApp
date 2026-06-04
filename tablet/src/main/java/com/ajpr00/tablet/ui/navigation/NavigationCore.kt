package com.ajpr00.tablet.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.ajpr00.tablet.presentation.viewmodel.MediaItemsViewModel
import com.ajpr00.tablet.presentation.viewmodel.ReproducorViewModel
import com.ajpr00.tablet.presentation.viewmodel.SplashViewModel
import com.ajpr00.tablet.ui.components.MenuApp
import com.ajpr00.tablet.ui.components.MenuGestoReproductor
import com.ajpr00.tablet.ui.components.Reproductor
import com.ajpr00.tablet.ui.components.ScreenMediaExplorer
import com.ajpr00.tablet.ui.screen.LoginScreenTablet
import com.ajpr00.tablet.ui.screen.MenuOverlayScreen
import com.ajpr00.tablet.ui.screen.RecoverPasswordTablet
import com.ajpr00.tablet.ui.screen.ReproductorScreen
import com.ajpr00.presentation_common.viewmodel.AuthViewModel
import com.ajpr00.presentation_common.viewmodel.LoginViewModel
import com.ajpr00.presentation_common.viewmodel.RegisterViewModel
import com.ajpr00.tablet.ui.screen.SplashScreenTablet

@Composable
fun NavigationCore(innerPadding: PaddingValues) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Splash, // Todo Cambiar al SplashScreen(ModoDebug)
        modifier = Modifier.padding(innerPadding)
    ) {
        composable<Splash> { backStackEntry ->
            val viewModelSplash: SplashViewModel = hiltViewModel()
            SplashScreenTablet(
                goToMainGraph = { navController.navigate(LoginGraph) },
            )
        }

        // Subgrafo de login
        navigation<LoginGraph>(startDestination = LoginScreen) {

            composable<LoginScreen> { backStackEntry ->
                val viewModelLoginViewModel: LoginViewModel = hiltViewModel()

                LoginScreenTablet(
                    modifier = Modifier,
                    viewModel = viewModelLoginViewModel,
                    goToMainGraph = { navController.navigate(MainGraph) },
                    goToFromRegistro = { navController.navigate(RegisterScreen) },
                    goToRecuperarPass = { navController.navigate(FromRecover) }
                )
            }
        }

        composable<RegisterScreen> { backStackEntry ->
            val viewModelLoginViewModel: RegisterViewModel = hiltViewModel()
            /*RegisterContent(
                modifier = Modifier,
                viewModel = viewModelLoginViewModel,
                goToBack = { navController.popBackStack() }
            )*/
        }
        composable<FromRecover> { backStackEntry ->
            val viewModelLoginViewModel: RegisterViewModel = hiltViewModel()
            RecoverPasswordTablet(
                viewModel = viewModelLoginViewModel,
                goToBack = { navController.popBackStack() }
            )
        }

        // Subgrafo Princial
        navigation<MainGraph>(startDestination = ScreenPrincipal) { // Cada subGrafo tiene su propio backStack

            composable<ScreenPrincipal> { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(MainGraph::class.qualifiedName!!)
                }
                val viewModelMediaBackground: ReproducorViewModel = hiltViewModel(parentEntry)
                val viewModelMediaItems: MediaItemsViewModel = hiltViewModel(parentEntry)
                val viewModelAuth: AuthViewModel = hiltViewModel(parentEntry)

                ReproductorScreen(
                    viewModelMediaBackground = viewModelMediaBackground,
                    viewModelMediaItems = viewModelMediaItems,
                    viewModelAuth = viewModelAuth,
                    goToLogin = { navController.navigate(LoginGraph) }
                )
            }
        }
    }
}

