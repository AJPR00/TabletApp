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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ajpr00.visumloop.tablet.R
import com.ajpr00.visumloop.tablet.presentation.viewmodel.SplashUiState
import com.ajpr00.visumloop.tablet.presentation.viewmodel.SplashViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@Composable
fun SplashScreen(
    goToLoginGraph: () -> Unit,
    goToMainGraph: () -> Unit,
    vm: SplashViewModel = hiltViewModel()
) {
    //val state by vm.uiState.collectAsState()

    // Animaciones visuales
    val scale = remember { Animatable(0f) }
    val alphaAnim = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, tween(2000, easing = FastOutSlowInEasing)) }
        launch { alphaAnim.animateTo(1f, tween(1500)) }
    }

    // Decisión de navegación
    LaunchedEffect(Unit) {
        delay(5000)
        when (SplashUiState.GoToMain) {
            SplashUiState.GoToLogin -> goToLoginGraph()
            SplashUiState.GoToMain -> goToMainGraph()
            else -> {} // Loading → solo muestra animación
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFE6BA)), contentAlignment = Alignment.Center) {
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