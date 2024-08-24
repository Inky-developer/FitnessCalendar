package com.inky.fitnesscalendar.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.emoji2.emojipicker.EmojiPickerView

@Composable
fun EmojiPickerDialog(onDismiss: () -> Unit, onEmoji: (String) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        EmojiPicker(onEmoji)
    }
}

@Composable
fun EmojiPicker(onEmoji: (String) -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 8.dp),
            factory = { context ->
                EmojiPickerView(context).apply {
                    setOnEmojiPickedListener { onEmoji(it.emoji) }
                }
            },
        )
    }
}

@Preview
@Composable
private fun PreviewEmojiPicker() {
    EmojiPicker(onEmoji = {})
}