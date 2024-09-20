package com.inky.fitnesscalendar.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.inky.fitnesscalendar.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OkayCancelDialog(
    onDismiss: () -> Unit,
    onOkay: () -> Unit,
    additionalButtons: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.padding(all = 8.dp)
        ) {
            Column {
                content()

                Row(modifier = Modifier.align(Alignment.End)) {
                    additionalButtons()

                    TextButton(onClick = onDismiss) {
                        Text(
                            stringResource(R.string.cancel),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    TextButton(onClick = onOkay) {
                        Text(
                            stringResource(R.string.ok),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}