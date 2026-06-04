package com.ajpr00.components.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun <T> ScreenMediaExplorer(
    items: List<T>,
    label: String = "",
    itemContent: @Composable (T) -> Unit
) {
    var animateIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animateIn = true
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AnimatedVisibility(
            visible = animateIn,
            modifier = Modifier.align(Alignment.CenterEnd),
            enter = slideInHorizontally(
                initialOffsetX = { full -> full },
                animationSpec = tween(durationMillis = 1500)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { full -> full },
                animationSpec = tween(durationMillis = 1500)
            )
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .border(
                        color = Color.Black,
                        width = 5.dp,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ),
            ) {
                Text(label, modifier = Modifier.align(Alignment.TopCenter))

                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items) { item ->
                        itemContent(item)
                    }
                }
            }
        }
    }
}
