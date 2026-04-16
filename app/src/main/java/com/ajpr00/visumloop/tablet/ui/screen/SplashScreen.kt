package com.ajpr00.visumloop.tablet.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.ajpr00.visumloop.tablet.R
import com.ajpr00.visumloop.tablet.presentation.viewmodel.SplashViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    goToMainGraph: () -> Unit,
) {
    val context = LocalContext.current

    // Animaciones visuales
    val scale = remember { Animatable(0f) }
    val alphaAnim = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, tween(2000, easing = FastOutSlowInEasing)) }
        launch { alphaAnim.animateTo(1f, tween(1500)) }
    }

    LaunchedEffect(Unit) {
        delay(1) //Todo cambio provisional para que se vea mejor
        goToMainGraph()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE6BA)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_splash),
            contentDescription = "Logo",
            modifier = Modifier
                .fillMaxSize(0.8f)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    alpha = alphaAnim.value
                }
        )
    }
}