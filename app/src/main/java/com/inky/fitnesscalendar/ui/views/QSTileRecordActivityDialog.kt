package com.inky.fitnesscalendar.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.RichRecording
import com.inky.fitnesscalendar.di.DecisionTrees
import com.inky.fitnesscalendar.di.DecisionTrees.classifyNow
import com.inky.fitnesscalendar.ui.components.ActivitySelector
import com.inky.fitnesscalendar.ui.components.ActivitySelectorState
import com.inky.fitnesscalendar.ui.components.OkayCancelRow
import com.inky.fitnesscalendar.ui.util.localDatabaseValues


/**
 * The recording dialog that is shown when the user taps on the quick settings tile
 */
@Composable
fun QsTileRecordActivityDialog(
    onDismiss: () -> Unit,
    onSave: (RichRecording) -> Unit
) {
    val context = LocalContext.current
    var state by remember {
        mutableStateOf(
            ActivitySelectorState(
                activityType = DecisionTrees.activityType?.classifyNow(context)
                    ?.takeIf { it.hasDuration },
                vehicle = DecisionTrees.vehicle?.classifyNow(context),
                place = null
            )
        )
    }
    val saveEnabled by remember { derivedStateOf { state.shouldSaveBeEnabled() } }
    val typeRows = localDatabaseValues.current.activityTypeRows
    val filteredTypeRows =
        remember(typeRows) { typeRows.map { row -> row.filter { it.hasDuration } } }

    Column(modifier = Modifier.padding(all = 16.dp)) {
        ActivitySelector(
            state = state,
            typeRows = filteredTypeRows,
            background = MaterialTheme.colorScheme.background,
            onState = { state = it }
        )

        OkayCancelRow(
            onNavigateBack = onDismiss,
            onSave = {
                onSave(state.toRecording()!!)
            },
            saveEnabled = saveEnabled,
            saveText = { Text(stringResource(R.string.action_record)) }
        )
    }
}