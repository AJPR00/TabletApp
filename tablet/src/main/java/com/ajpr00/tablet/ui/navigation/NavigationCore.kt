package com.ajpr00.tablet.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.ajpr00.tablet.presentation.viewmodel.MediaItemsViewModel
import com.ajpr00.tablet.presentation.viewmodel.ReproducorViewModel
import com.ajpr00.tablet.ui.screen.LoginScreenTablet
import com.ajpr00.tablet.ui.screen.RecoverPasswordTablet
import com.ajpr00.tablet.ui.screen.ReproductorScreen
import com.ajpr00.presentation_common.viewmodel.AuthViewModel
import com.ajpr00.presentation_common.viewmodel.LoginViewModel
import com.ajpr00.presentation_common.viewmodel.RegisterViewModel
import com.ajpr00.tablet.presentation.viewmodel.MainGraphViewModel
import com.ajpr00.tablet.presentation.viewmodel.OnboardingTabletViewModel
import com.ajpr00.tablet.presentation.viewmodel.PairingViewModel
import com.ajpr00.tablet.presentation.viewmodel.SettingsViewModel
import com.ajpr00.tablet.presentation.viewmodel.SplashTabletViewModel
import com.ajpr00.tablet.ui.components.StartServerOnce
import com.ajpr00.tablet.ui.screen.SettingsScreen
import com.ajpr00.tablet.ui.screen.SplashScreenTablet
import com.ajpr00.tablet.ui.screen.onboarding.OnboardingInfoScreenTablet
import com.ajpr00.tablet.ui.screen.onboarding.OnboardingReadyScreenTablet
import com.ajpr00.tablet.ui.screen.onboarding.OnboardingWelcomeScreenTablet

@Composable
fun NavigationCore(
    innerPadding: PaddingValues,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    onFacebookLogin: () -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Splash, // Todo Cambiar al SplashScreen(ModoDebug)
        modifier = Modifier.padding(innerPadding)
    ) {
        composable<Splash> {
            val viewModelSplash: SplashTabletViewModel = hiltViewModel()
            SplashScreenTablet(
                viewModel = viewModelSplash,
                goToOnboarding = { navController.navigate(OnboardinGraph) },
                goToMainGraph = { navController.navigate(LoginGraph) },
            )
        }

        composable<About> {}
        navigation<SettingsGraph>(startDestination = Settings) {

            composable<Settings> {
                SettingsScreen(settingsViewModel)
            }
        }

        // Subgrafo de login
        navigation<LoginGraph>(startDestination = LoginScreen) {

            composable<LoginScreen> {
                val viewModelLoginViewModel: LoginViewModel = hiltViewModel()

                LoginScreenTablet(
                    modifier = Modifier,
                    viewModelAuth = authViewModel,
                    onFacebookLogin = onFacebookLogin,
                    viewModelLogin = viewModelLoginViewModel,
                    goToMainGraph = { navController.navigate(MainGraph) },
                    goToFromRegistro = { navController.navigate(RegisterScreen) },
                    goToRecuperarPass = { navController.navigate(FromRecover) }
                )
            }
        }

        composable<RegisterScreen> {
            val viewModelLoginViewModel: RegisterViewModel = hiltViewModel()
            /*RegisterContent(
                modifier = Modifier,
                viewModel = viewModelLoginViewModel,
                goToBack = { navController.popBackStack() }
            )*/
        }
        composable<FromRecover> {
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

                val vmMain: MainGraphViewModel = hiltViewModel(parentEntry)
                val isFirstRun by vmMain.isFirstRun.collectAsState()
                val context = LocalContext.current

                if (!isFirstRun) {
                    StartServerOnce(context)
                }

                val viewModelMediaBackground: ReproducorViewModel = hiltViewModel(parentEntry)
                val viewModelMediaItems: MediaItemsViewModel = hiltViewModel(parentEntry)
                val viewModelAuth: AuthViewModel = hiltViewModel(parentEntry)
                val viewModelPairing: PairingViewModel = hiltViewModel(parentEntry)

                ReproductorScreen(
                    viewModelMediaBackground = viewModelMediaBackground,
                    viewModelMediaItems = viewModelMediaItems,
                    viewModelPairing = viewModelPairing,
                    viewModelAuth = viewModelAuth,
                    goToLogin = { navController.navigate(LoginGraph) },
                    goToSetting = {navController.navigate(Settings)}
                )
            }
        }

        navigation<OnboardinGraph>(startDestination = OnboardingWelcomeScreenTablet) {

            composable<OnboardingWelcomeScreenTablet> { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(OnboardinGraph::class.qualifiedName!!)
                }
                val vm: OnboardingTabletViewModel = hiltViewModel(parentEntry)

                OnboardingWelcomeScreenTablet(
                    onNext = { navController.navigate(OnboardingInfoScreenTablet) }
                )
            }

            composable<OnboardingInfoScreenTablet> { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(OnboardinGraph::class.qualifiedName!!)
                }
                val vm: OnboardingTabletViewModel = hiltViewModel(parentEntry)

                OnboardingInfoScreenTablet(
                    onNext = {
                        vm.saveNameProvisonal(it)
                        navController.navigate(OnboardingReadyScreenTablet)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<OnboardingReadyScreenTablet> { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(OnboardinGraph::class.qualifiedName!!)
                }
                val vm: OnboardingTabletViewModel = hiltViewModel(parentEntry)

                OnboardingReadyScreenTablet(
                    tabletName = vm.name,
                    onFinish = {
                        vm.setTabletName(vm.name)
                        navController.navigate(LoginGraph)
                    }
                )
            }
        }
    }
}

