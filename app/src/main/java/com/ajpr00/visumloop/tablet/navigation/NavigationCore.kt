package com.ajpr00.visumloop.tablet.navigation

import android.util.Log
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
import androidx.navigation.toRoute
import com.ajpr00.visumloop.tablet.presentation.viewmodel.LoginViewModel
import com.ajpr00.visumloop.tablet.presentation.viewmodel.ReproducorViewModel
import com.ajpr00.visumloop.tablet.presentation.viewmodel.MediaItemsViewModel
import com.ajpr00.visumloop.tablet.presentation.viewmodel.RegisterViewModel
import com.ajpr00.visumloop.tablet.ui.components.FromRegister
import com.ajpr00.visumloop.tablet.ui.components.MediaList
import com.ajpr00.visumloop.tablet.ui.components.MenuGestoReproductor
import com.ajpr00.visumloop.tablet.ui.components.MenuApp
import com.ajpr00.visumloop.tablet.ui.components.Reproductor
import com.ajpr00.visumloop.tablet.ui.screen.FromRecover
import com.ajpr00.visumloop.tablet.ui.screen.MenuOverlayScreen
import com.ajpr00.visumloop.tablet.ui.screen.ReproductorScreen
import com.ajpr00.visumloop.tablet.ui.screen.SplashScreen
import com.ajpr00.visumloop.tablet.ui.screen.LoginScreen

@Composable
fun NavigationCore(innerPadding: PaddingValues) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Splash, // Todo Cambiar al SplashScreen(ModoDebig)
        modifier = Modifier.padding(innerPadding)
    ) {
        composable<Splash> { backStackEntry ->
            SplashScreen(
                goToMainGraph = { navController.navigate(MainGraph) },
            )
        }

        // Subgrafo de login
        navigation<LoginGraph>(startDestination = LoginScreen) {

            composable<LoginScreen> { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(LoginGraph::class)
                }

                val args = parentEntry.toRoute<LoginGraph>()
                Log.d("LoginGraph", "args: ${args.acessType}")

                val viewModelLoginViewModel: LoginViewModel = hiltViewModel()

                LoginScreen(
                    modifier = Modifier,
                    viewModel = viewModelLoginViewModel,
                    accesLoginType = args.acessType,
                    goToMainGraph = { navController.navigate(MainGraph) },
                    goToFromRegistro = { navController.navigate(RegisterScreen) },
                    goToRecuperarPass = { navController.navigate(FromRecover) }
                )
            }
        }

        composable<RegisterScreen> { backStackEntry ->
            val viewModelLoginViewModel: RegisterViewModel = hiltViewModel()
            FromRegister(
                modifier = Modifier,
                viewModel = viewModelLoginViewModel,
                goToBack = { navController.popBackStack() }
            )
        }
        composable<FromRecover> { backStackEntry ->
            val viewModelLoginViewModel: RegisterViewModel = hiltViewModel()
            FromRecover(
                viewModel = viewModelLoginViewModel,
                goToBack = { navController.popBackStack() }
            )
        }


        navigation<MainGraph>(startDestination = ScreenPrincipal) { // Se crea un nuevo backStack ajeno al principal

            composable<ScreenPrincipal> { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(MainGraph::class.qualifiedName!!)
                }
                val viewModelMediaBackground: ReproducorViewModel = hiltViewModel(parentEntry)
                val viewModelMediaItems: MediaItemsViewModel = hiltViewModel(parentEntry)

                ReproductorScreen(
                    goToReproductor = { Reproductor(viewModelMediaBackground) },
                    goToMenuOverlayScreen = {
                        MenuOverlayScreen(
                            viewModelMediaBackground = viewModelMediaBackground,
                            menuGestoReproducion = {
                                MenuGestoReproductor(
                                    isVideo = viewModelMediaBackground.isVideo.collectAsState().value,
                                    onPaused = { viewModelMediaBackground.togglePlayPause() },
                                    onPrevMedia = { viewModelMediaBackground.prevMedia() },
                                    onNextMedia = { viewModelMediaBackground.nextMedia() },
                                    onRewind = { viewModelMediaBackground.rewind() },
                                    onForward = { viewModelMediaBackground.forward() },
                                    onSeekForward = { delta ->
                                        viewModelMediaBackground.forward(
                                            delta
                                        )
                                    },
                                    onSeekBackward = { delta ->
                                        viewModelMediaBackground.rewind(
                                            delta
                                        )
                                    },
                                    onVolumeChange = { delta ->
                                        viewModelMediaBackground.setVolume(
                                            viewModelMediaBackground.option.value.volume + delta
                                        )
                                    },
                                    onLockScreen = { viewModelMediaBackground.togglesLockScreen() },
                                    onShowMenu = { viewModelMediaBackground.isShowMenuApp(it) },
                                    resetTimer = { viewModelMediaBackground.updateLastInteraction() }
                                )
                            },
                            menuApp = {
                                MenuApp(
                                    viewModelMediaBackground = viewModelMediaBackground,
                                    viewModelMediaItme = viewModelMediaItems,
                                    goToLogin = { navController.navigate(LoginGraph(acessType = it)) },
                                )
                            },
                            panelSelectorMedia = {
                                MediaList(
                                    viewModelMediaItems = viewModelMediaItems,
                                    viewModelMediaBackground = viewModelMediaBackground,

                                    )
                            }
                        )
                    }
                )
            }
        }
    }
}

