package com.inky.fitnesscalendar.ui.util

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

/**
 * Creates an animation that animates items coming in left or right, depending on whether they are
 * bigger or smaller than the last value.
 */
fun <T : Comparable<T>> horizontalOrderedTransitionSpec(): AnimatedContentTransitionScope<T?>.() -> ContentTransform =
    {
        val left = AnimatedContentTransitionScope.SlideDirection.Left
        val right = AnimatedContentTransitionScope.SlideDirection.Right
        if (targetState == null || initialState == null) {
            fadeIn() togetherWith fadeOut()
        } else if (targetState!! > initialState!!) {
            slideIntoContainer(left) togetherWith slideOutOfContainer(left)
        } else {
            slideIntoContainer(right) togetherWith slideOutOfContainer(right)
        }
    }