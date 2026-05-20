package com.ajpr00.tablet.presentation.transitions

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

sealed class TransitionOption {
    data class Fade(val duration: Int = 500) : TransitionOption()
    data class Slide(val duration: Int = 500, val fromLeft: Boolean = true) : TransitionOption()
    data class Scale(val duration: Int = 500, val factor: Float = 1.2f) : TransitionOption()
}
fun transitionFor(option: TransitionOption): AnimatedContentTransitionScope<*>.() -> ContentTransform = {
    when (option) {
        is TransitionOption.Fade -> ContentTransform(
            targetContentEnter = fadeIn(animationSpec = tween(option.duration)),
            initialContentExit = fadeOut(animationSpec = tween(option.duration))
        )

        is TransitionOption.Slide -> if (option.fromLeft) {
            ContentTransform(
                targetContentEnter = slideInHorizontally(animationSpec = tween(option.duration)) { -it },
                initialContentExit = slideOutHorizontally(animationSpec = tween(option.duration)) { it }
            )
        } else {
            ContentTransform(
                targetContentEnter = slideInHorizontally(animationSpec = tween(option.duration)) { it },
                initialContentExit = slideOutHorizontally(animationSpec = tween(option.duration)) { -it }
            )
        }

        is TransitionOption.Scale -> ContentTransform(
            targetContentEnter = scaleIn(animationSpec = tween(option.duration), initialScale = option.factor),
            initialContentExit = scaleOut(animationSpec = tween(option.duration), targetScale = option.factor)
        )
    }
}
