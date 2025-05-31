package com.inky.fitnesscalendar.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.inky.fitnesscalendar.R

@Composable
fun BaseEditDialog(
    title: String,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean = true,
    actions: @Composable ColumnScope.() -> Unit,
    saveText: @Composable () -> Unit = { Text(stringResource(R.string.save)) },
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onNavigateBack,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        ) {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentSize()
                            .animateContentSize()
                    )

                    Column {
                        actions()
                    }
                }
                HorizontalDivider()
            }

            Box(modifier = Modifier.weight(1f, fill = false)) {
                content()
            }

            OkayCancelRow(
                onNavigateBack = onNavigateBack,
                onSave = onSave,
                saveEnabled = saveEnabled,
                saveText = saveText
            )
        }
    }
}

@Composable
fun ColumnScope.OkayCancelRow(
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean,
    saveText: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.align(Alignment.End)) {
        TextButton(onClick = onNavigateBack) {
            Text(stringResource(R.string.cancel))
        }
        TextButton(
            onClick = onSave,
            enabled = saveEnabled,
            modifier = Modifier.testTag("button-confirm")
        ) {
            saveText()
        }
    }
}