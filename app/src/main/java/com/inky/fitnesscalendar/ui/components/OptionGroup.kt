package com.inky.fitnesscalendar.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OptionGroup(
    modifier: Modifier = Modifier,
    label: String,
    selectionLabel: String? = null,
    content: @Composable () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
    ) {
        Column {
            Text(
                selectionLabel ?: label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selectionLabel != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 4.dp)
            )
            content()
        }
    }
}