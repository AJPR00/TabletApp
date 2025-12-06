package com.ajpr00.visumloop.tablet.ui.screen

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
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        goToReproductor()
        Log.d("ReproductorScreen", "gotoReproductor()")

        goToMenuOverlayScreen()
        Log.d("ReproductorScreen", "showMenu activo → navegamos a MenuOverlay")
    }
}