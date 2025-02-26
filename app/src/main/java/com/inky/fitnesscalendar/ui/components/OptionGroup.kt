package com.inky.fitnesscalendar.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun optionGroupDefaultBackground() = MaterialTheme.colorScheme.surfaceContainer

@Composable
fun OptionGroup(
    modifier: Modifier = Modifier,
    label: String,
    selectionLabel: String? = null,
    background: Color = optionGroupDefaultBackground(),
    content: @Composable () -> Unit
) {
    Surface(
        color = background,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                LabelText(label)
                AnimatedContent(selectionLabel) { label ->
                    if (label != null) {
                        LabelText(label, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun LabelText(text: String, color: Color = MaterialTheme.colorScheme.secondary) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
    )
}