package com.ajpr00.mobile.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.ajpr00.visumloop.mobile.R
import com.ajpr00.components.screen.SplashScreenContent
import com.ajpr00.presentation_common.viewmodel.SplashViewModel

@Composable
fun SplashScreenMobile(
    viewModel: SplashViewModel,
    onNavigateToMain: () -> Unit,
    onNavigateToLogin: () -> Unit,
    ) {

    SplashScreenContent(
        splashImage = painterResource(id = R.drawable.imag_splash_control),
        onAnimationFinished = {
            (if (viewModel.isLoggedIn.value) onNavigateToMain else onNavigateToLogin)()
        }
    )
}
