package com.inky.fitnesscalendar.ui.util

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.inky.fitnesscalendar.ui.SharedTransition

open class SharedContentKey {
    data class ActivityCard(val id: Int?) : SharedContentKey()

    object NewActivityFAB : SharedContentKey()

    object AppBar : SharedContentKey()
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedBounds(key: SharedContentKey) =
    with(SharedTransition.current.sharedTransitionScope) {
        this@sharedBounds.sharedBounds(
            rememberSharedContentState(key = key),
            animatedVisibilityScope = SharedTransition.current.animatedContentScope
        )
    }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedElement(key: SharedContentKey) =
    with(SharedTransition.current.sharedTransitionScope) {
        this@sharedElement.sharedElement(
            rememberSharedContentState(key = key),
            animatedVisibilityScope = SharedTransition.current.animatedContentScope
        )
    }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.renderInSharedTransitionScopeOverlay() =
    with(SharedTransition.current.sharedTransitionScope) {
        this@renderInSharedTransitionScopeOverlay.renderInSharedTransitionScopeOverlay()
    }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.skipToLookaheadSize() =
    with(SharedTransition.current.sharedTransitionScope) {
        this@skipToLookaheadSize.skipToLookaheadSize()
    }