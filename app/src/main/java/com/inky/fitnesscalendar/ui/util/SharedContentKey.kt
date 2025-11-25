package com.inky.fitnesscalendar.ui.util

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.inky.fitnesscalendar.ui.localSharedTransition

sealed class SharedContentKey {
    data class ActivityCard(val id: Int?) : SharedContentKey()

    data object ActivityImage : SharedContentKey()

    data object NewActivityFAB : SharedContentKey()

    data object AppBar : SharedContentKey()

    data object DayImage : SharedContentKey()

    data object DayDescription : SharedContentKey()

    data object DayFeel : SharedContentKey()

    data object DayTitle : SharedContentKey()

    data object Map : SharedContentKey()

    data object FilterInformation : SharedContentKey()

    data object NoActivitiesInfoBox : SharedContentKey()
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedBounds(key: SharedContentKey) =
    with(localSharedTransition.current.sharedTransitionScope) {
        this@sharedBounds.sharedBounds(
            rememberSharedContentState(key = key),
            animatedVisibilityScope = localSharedTransition.current.animatedContentScope
        )
    }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedElement(key: SharedContentKey) =
    with(localSharedTransition.current.sharedTransitionScope) {
        this@sharedElement.sharedElement(
            rememberSharedContentState(key = key),
            animatedVisibilityScope = localSharedTransition.current.animatedContentScope
        )
    }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.renderInSharedTransitionScopeOverlay() =
    with(localSharedTransition.current.sharedTransitionScope) {
        this@renderInSharedTransitionScopeOverlay.renderInSharedTransitionScopeOverlay()
    }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.skipToLookaheadSize() =
    with(localSharedTransition.current.sharedTransitionScope) {
        this@skipToLookaheadSize.skipToLookaheadSize()
    }