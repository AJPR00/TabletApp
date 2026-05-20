package com.ajpr00.tablet.ui.screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@SuppressLint("MultipleAwaitPointerEventScopes")
@Composable
fun ReproductorScreen(
    modifier: Modifier = Modifier,
    goToReproductor: @Composable () -> Unit,
    goToMenuOverlayScreen: @Composable () -> Unit
) {
    val TAG = "ReproductorScreen"

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        goToReproductor()
        Log.d(TAG, "showReproductor activo → navegamos a Reproductor")

        goToMenuOverlayScreen()
        Log.d(TAG, "showMenu activo → navegamos a MenuOverlay")
    }
}