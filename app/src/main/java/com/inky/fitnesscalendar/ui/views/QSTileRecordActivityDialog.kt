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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.Recording
import com.inky.fitnesscalendar.db.entities.TypeRecording
import com.inky.fitnesscalendar.di.DecisionTrees
import com.inky.fitnesscalendar.ui.components.ActivitySelector
import com.inky.fitnesscalendar.ui.components.ActivitySelectorState
import com.inky.fitnesscalendar.ui.components.OkayCancelRow
import com.inky.fitnesscalendar.ui.util.localDatabaseValues
import java.time.Instant
import java.util.Date


/**
 * The recording dialog that is shown when the user taps on the quick settings tile
 */
@Composable
fun QsTileRecordActivityDialog(
    onDismiss: () -> Unit,
    onSave: (TypeRecording) -> Unit
) {
    var state by remember {
        mutableStateOf(
            ActivitySelectorState(
                activityType = DecisionTrees.activityType?.classifyNow()?.takeIf { it.hasDuration },
                vehicle = DecisionTrees.vehicle?.classifyNow()
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
            onActivityType = { state = state.copy(activityType = it) },
            onVehicle = { state = state.copy(vehicle = it) },
            background = MaterialTheme.colorScheme.background
        )

        OkayCancelRow(
            onNavigateBack = onDismiss,
            onSave = {
                onSave(
                    TypeRecording(
                        recording = Recording(
                            typeId = state.activityType?.uid!!,
                            vehicle = state.vehicle,
                            startTime = Date.from(
                                Instant.now()
                            )
                        ),
                        type = state.activityType!!
                    )
                )
            },
            saveEnabled = saveEnabled,
            saveText = { Text(stringResource(R.string.action_record)) }
        )
    }
}