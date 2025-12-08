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
import com.ajpr00.visumloop.tablet.presentation.viewmodel.MediaItemsViewModel
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
                val viewModelMediaBackground: MediaBackgroundViewModel = hiltViewModel(parentEntry)
                val viewModelMediaItems: MediaItemsViewModel = hiltViewModel(parentEntry)

                ReproductorScreen(
                    goToReproductor = { Reproductor(viewModelMediaBackground) },
                    goToMenuOverlayScreen = {
                        MenuOverlayScreen(
                            viewModelMediaBackground = viewModelMediaBackground,
                            menuGestoReproducion = {
                                MenuGesto(
                                    isVideo = viewModelMediaBackground.isVideo.collectAsState().value,
                                    onPaused = { viewModelMediaBackground.togglePlayPause() },
                                    onPrevMedia = { viewModelMediaBackground.prevMedia() },
                                    onNextMedia = { viewModelMediaBackground.nextMedia() },
                                    onRewind = { viewModelMediaBackground.rewind()},
                                    onForward = { viewModelMediaBackground.forward()},
                                    onSeekForward = { delta -> viewModelMediaBackground.forward(delta) },
                                    onSeekBackward = { delta -> viewModelMediaBackground.rewind(delta) },
                                    onVolumeChange = { delta -> viewModelMediaBackground.setVolume(viewModelMediaBackground.option.value.volume + delta) },
                                    onLockScreen = { viewModelMediaBackground.togglesLockScreen() },
                                    onShowMenu = { viewModelMediaBackground.isShowMenuApp(it) },
                                    resetTimer = { viewModelMediaBackground.updateLastInteraction() }
                                )
                            },
                            menuApp = {
                                MenuApp(
                                    viewModelMediaBackground = viewModelMediaBackground,
                                    viewModelAuth = hiltViewModel(),
                                    viewModelMediaItme = viewModelMediaItems
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

