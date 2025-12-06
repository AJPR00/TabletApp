package com.ajpr00.visumloop.tablet.navigation

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
import com.ajpr00.visumloop.tablet.presentation.viewmodel.MediaBackgroundViewModel
import com.ajpr00.visumloop.tablet.ui.components.MediaList
import com.ajpr00.visumloop.tablet.ui.components.MenuGesto
import com.ajpr00.visumloop.tablet.ui.components.MenuApp
import com.ajpr00.visumloop.tablet.ui.components.Reproductor
import com.ajpr00.visumloop.tablet.ui.screen.MenuOverlayScreen
import com.ajpr00.visumloop.tablet.ui.screen.ReproductorScreen
import com.ajpr00.visumloop.tablet.ui.screen.SplashScreen

@Composable
fun NavigationCore(innerPadding: PaddingValues) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Splash, // navGraph principal
        modifier = Modifier.padding(innerPadding)
    ) {
        composable<Splash> { backStackEntry ->
            SplashScreen(
                goToLoginGraph = { navController.navigate(LoginGraph) },
                goToMainGraph = { navController.navigate(MainGraph) },
            )
        }

        // Subgrafo de login
        navigation<LoginGraph>(startDestination = LoginScreen) {
            composable<LoginScreen> {
                /*LoginScreen(
                    onLoginSuccess = { navController.navigate(PlayerGraph) },
                    onRegister = { navController.navigate(RegisterScreen) }
                )*/
            }
            composable<RegisterScreen> { // RegisterScreen()
            }
            composable<ForgotPasswordScreen> {// ForgotPasswordScreen()
            }
        }

        navigation<MainGraph>(startDestination = ScreenPrincipal) { // Se crea un nuevo backStack ajeno al principal

            composable<ScreenPrincipal> { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(MainGraph::class.qualifiedName!!)
                }
                val vm: MediaBackgroundViewModel = hiltViewModel(parentEntry)

                ReproductorScreen(
                    goToReproductor = { Reproductor(vm) },
                    goToMenuOverlayScreen = {
                        MenuOverlayScreen(
                            viewModelMediaBackground = vm,
                            menuGestoReproducion = {
                                MenuGesto(
                                    isVideo = vm.isVideo.collectAsState().value,
                                    onPaused = { vm.togglePlayPause() },
                                    onPrevMedia = { vm.prevMedia() },
                                    onNextMedia = { vm.nextMedia() },
                                    onRewind = { vm.rewind() },
                                    onForward = { vm.forward() },
                                    onSeekForward = { vm.forward() },
                                    onSeekBackward = { vm.rewind() },
                                    onVolumeChange = { delta -> vm.setVolume(vm.option.value.volume + delta)},
                                    onLock = { vm.toggleLockScreen() },
                                    onMenu = { vm.toggleShowMenuApp() },
                                    resetTimer = { vm.resetTimer() }
                                )
                            },
                            menuApp = {
                                MenuApp(
                                    viewModelMediaBackground = vm,
                                    viewModelAuth = hiltViewModel(),
                                    viewModelMediaItme = hiltViewModel()
                                )
                            },
                            panelSelectorMedia = {
                                MediaList(
                                    items = vm.mediaList.collectAsState().value,
                                    onItemClick = { /* acción */ },
                                    onToggleFavorite = { /* acción */ }
                                )
                            }
                        )
                    }
                )
            }
        }
    }
}

