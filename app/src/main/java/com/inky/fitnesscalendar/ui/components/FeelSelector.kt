package com.inky.fitnesscalendar.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.data.Feel

@Composable
fun FeelSelector(feel: Feel?, onChange: (Feel?) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        for (feelEntry in Feel.entries) {
            InputChip(
                selected = feel == feelEntry,
                onClick = {
                    val newFeel = if (feel == feelEntry) {
                        null
                    } else {
                        feelEntry
                    }
                    onChange(newFeel)
                },
                label = { Text(feelEntry.emoji, style = MaterialTheme.typography.headlineMedium) },
                modifier = Modifier.padding(all = 4.dp)
            )
        }
    }
}

@Preview("Feel")
@Composable
fun FeelPreview() {
    var feel by remember {
        mutableStateOf<Feel?>(Feel.Ok)
    }
    FeelSelector(feel = feel, onChange = { feel = it })
}