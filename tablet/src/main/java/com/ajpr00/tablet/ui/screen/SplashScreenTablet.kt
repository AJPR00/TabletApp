package com.ajpr00.tablet.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.ajpr00.tablet.R
import com.ajpr00.components.screen.SplashScreenContent

@Composable
fun SplashScreenTablet(    goToMainGraph: () -> Unit
) {
    SplashScreenContent(
        splashImage = painterResource(id = R.drawable.img_splash_tablet),
        onAnimationFinished = { goToMainGraph() }
    )
}
