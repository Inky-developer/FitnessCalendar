package com.inky.fitnesscalendar.ui.components

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.ui.util.SharedContentKey

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NewActivityFAB(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onClick: () -> Unit,
    menuOpen: Boolean
) {
    val angle = animateFloatAsState(
        targetValue = if (menuOpen) {
            45f
        } else {
            0f
        }, label = "fab_rotation"
    )
    with(sharedTransitionScope) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .sharedElement(
                    rememberSharedContentState(key = SharedContentKey.NewActivityFAB),
                    animatedVisibilityScope = animatedContentScope
                )
                .renderInSharedTransitionScopeOverlay()
        ) {
            Icon(
                Icons.Filled.Add,
                stringResource(R.string.action_new_activity),
                modifier = Modifier.rotate(angle.value)
            )
        }
    }
}