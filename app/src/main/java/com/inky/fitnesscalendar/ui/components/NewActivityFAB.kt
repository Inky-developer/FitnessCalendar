package com.inky.fitnesscalendar.ui.components

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.ui.util.Icons
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.renderInSharedTransitionScopeOverlay
import com.inky.fitnesscalendar.ui.util.sharedElement

@Composable
fun NewActivityFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .sharedElement(SharedContentKey.NewActivityFAB)
            .renderInSharedTransitionScopeOverlay()
    ) {
        Icons.Add(stringResource(R.string.action_new_activity))
    }
}