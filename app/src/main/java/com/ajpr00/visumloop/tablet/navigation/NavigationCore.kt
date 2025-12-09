package com.ajpr00.visumloop.tablet.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.ajpr00.visumloop.tablet.presentation.viewmodel.LoginViewModel
import com.ajpr00.visumloop.tablet.presentation.viewmodel.MediaBackgroundViewModel
import com.ajpr00.visumloop.tablet.presentation.viewmodel.MediaItemsViewModel
import com.ajpr00.visumloop.tablet.presentation.viewmodel.RegisterViewModel
import com.ajpr00.visumloop.tablet.ui.components.FromRegister
import com.ajpr00.visumloop.tablet.ui.components.MediaList
import com.ajpr00.visumloop.tablet.ui.components.MenuGestoReproductor
import com.ajpr00.visumloop.tablet.ui.components.MenuApp
import com.ajpr00.visumloop.tablet.ui.components.PanelMenuOpciones
import com.ajpr00.visumloop.tablet.ui.components.Reproductor
import com.ajpr00.visumloop.tablet.ui.screen.MenuOverlayScreen
import com.ajpr00.visumloop.tablet.ui.screen.ReproductorScreen
import com.ajpr00.visumloop.tablet.ui.screen.SplashScreen
import com.ajpr00.visumloop.tablet.ui.screen.LoginScreen

@Composable
fun NavigationCore(innerPadding: PaddingValues) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LoginGraph, // Todo Cambiar al SplashScreen(ModoDebig)
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
                    navController.getBackStackEntry(LoginGraph::class.qualifiedName!!)
                }
                val viewModelLoginViewModel: LoginViewModel = hiltViewModel(parentEntry)
                LoginScreen(
                    modifier = Modifier,
                    viewModel = viewModelLoginViewModel,
                    goToMainGraph = { navController.navigate(MainGraph) },
                    goToFromRegistro = { navController.navigate(RegisterScreen) },
                    goToRecuperarPass = { navController.navigate(ForgotPasswordScreen) }
                )
            }
            composable<RegisterScreen> { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(LoginGraph::class.qualifiedName!!)
                }
                val viewModelLoginViewModel: RegisterViewModel = hiltViewModel(parentEntry)
                FromRegister(
                    modifier = Modifier,
                    viewModel = viewModelLoginViewModel,
                    goToBack = { navController.popBackStack() }
                )
            }
            composable<ForgotPasswordScreen> { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(LoginGraph::class.qualifiedName!!)
                }
                val viewModelLoginViewModel: LoginViewModel = hiltViewModel(parentEntry)
                //FromForgotPassword(viewModel = viewModelLoginViewModel)
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
                                    viewModelAuth = hiltViewModel(),
                                    viewModelMediaItme = viewModelMediaItems,
                                    panelContent = {
                                        PanelMenuOpciones(
                                            shape = RoundedCornerShape(10),
                                            configuracion = { /*TODO*/ },
                                            info = { /*TODO*/ },
                                            login = { navController.navigate(LoginGraph) },
                                            ftp = { /*TODO*/ },
                                            vacio = { /*TODO*/ },
                                            dropBox = { viewModelMediaBackground.isShowSidePanel(it) },
                                            misArchivos = { },
                                            favorito = { viewModelMediaBackground.isShowSidePanel(it) },
                                            googleDrive = {
                                                viewModelMediaBackground.isShowSidePanel(
                                                    it
                                                )
                                            },
                                            onLock = { viewModelMediaBackground.togglesLockScreen() },
                                            onClosedMenuApp = {
                                                viewModelMediaBackground.isShowMenuApp(
                                                    it
                                                )
                                            }
                                        )
                                    }
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

