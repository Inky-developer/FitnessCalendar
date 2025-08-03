package com.inky.fitnesscalendar.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.RichRecording
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.components.OkayCancelRow
import com.inky.fitnesscalendar.ui.components.optionGroupDefaultBackground


/**
 * The recording dialog that is shown when the user taps on the quick settings tile
 */
@Composable
fun QsTileRecordActivityDialog(
    localizationRepository: LocalizationRepository,
    onDismiss: () -> Unit,
    onSave: (RichRecording) -> Unit
) {
    var state by rememberSaveable { mutableStateOf(RecordActivityState()) }

    Column(
        modifier = Modifier
            .background(optionGroupDefaultBackground())
            .padding(all = 16.dp)
    ) {
        RecordActivityInner(
            state = state,
            localizationRepository = localizationRepository,
            onState = { state = it },
            includeTimePicker = false,
        )

        OkayCancelRow(
            onNavigateBack = onDismiss,
            onSave = {
                onSave(state.toRecording()!!)
            },
            saveEnabled = state.isValid,
            saveText = { Text(stringResource(R.string.action_record)) }
        )
    }
}