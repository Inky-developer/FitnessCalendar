package com.inky.fitnesscalendar.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.RichRecording
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.components.OkayCancelRow
import com.inky.fitnesscalendar.ui.util.localDatabaseValues


/**
 * The recording dialog that is shown when the user taps on the quick settings tile
 */
@Composable
fun QsTileRecordActivityDialog(
    localizationRepository: LocalizationRepository,
    onDismiss: () -> Unit,
    onSave: (RichRecording) -> Unit
) {
    val context = LocalContext.current
    var state by rememberSaveable {
        mutableStateOf(RecordActivityState.fromPrediction(context))
    }
    val typeRows = localDatabaseValues.current.activityTypeRows
    val filteredTypeRows =
        remember(typeRows) { typeRows.map { row -> row.filter { it.hasDuration } } }

    Column(modifier = Modifier.padding(all = 16.dp)) {
        RecordActivityInner(
            state = state,
            typeRows = filteredTypeRows,
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