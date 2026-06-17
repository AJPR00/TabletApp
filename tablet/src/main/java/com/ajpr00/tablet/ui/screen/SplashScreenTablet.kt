package com.ajpr00.tablet.ui.screen

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import com.ajpr00.tablet.R
import com.ajpr00.components.screen.SplashScreenContent
import com.ajpr00.presentation_common.viewmodel.AuthViewModel
import com.ajpr00.tablet.presentation.viewmodel.SplashTabletViewModel

@Composable
fun SplashScreenTablet(
    viewModel: SplashTabletViewModel,
    viewModelAuth: AuthViewModel,
    goToOnboarding: () -> Unit,
    goToMainGraph: () -> Unit,
    goToLogin: () -> Unit
) {

    val isFirstRun by viewModel.isFirstRun.collectAsState()
    val isLoggedIn by viewModelAuth.isLoggedIn.collectAsState()

    Log.d("SplashScreenTablet", "firstRunCompleted: $isFirstRun")
    Log.d("SplashScreenTablet", "isLoggedIn: $isLoggedIn")


    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == true) {
            viewModelAuth.registerTabletInFirestore()
        }
    }

    SplashScreenContent(
        splashImage = painterResource(id = R.drawable.img_splash_tablet),
        onAnimationFinished = {

            if (isLoggedIn == null || isFirstRun == null) {
                return@SplashScreenContent
            }
            when {
                isFirstRun == true -> goToOnboarding()
                isLoggedIn == true -> goToMainGraph()
                else -> goToLogin()
            }
        }
    )
}

