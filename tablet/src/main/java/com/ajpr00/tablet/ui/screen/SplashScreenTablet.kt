package com.ajpr00.tablet.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ajpr00.tablet.R
import com.ajpr00.components.screen.SplashScreenContent
import com.ajpr00.tablet.presentation.viewmodel.SplashTabletViewModel

@Composable
fun SplashScreenTablet(
    viewModel: SplashTabletViewModel,
    goToOnboarding: () -> Unit,
    goToMainGraph: () -> Unit
) {

    val firstRunCompleted by viewModel.firstRunCompleted.collectAsState()

    SplashScreenContent(
        splashImage = painterResource(id = R.drawable.img_splash_tablet),
        onAnimationFinished = {
            if (firstRunCompleted) goToOnboarding()
            else goToMainGraph()
        }
    )
}

