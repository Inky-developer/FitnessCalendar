package com.inky.fitnesscalendar.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.Recording
import com.inky.fitnesscalendar.db.entities.TypeRecording
import com.inky.fitnesscalendar.ui.components.ActivitySelector
import com.inky.fitnesscalendar.ui.components.ActivitySelectorState
import com.inky.fitnesscalendar.ui.components.OkayCancelRow
import java.time.Instant
import java.util.Date


/**
 * The recording dialog that is shown when the user taps on the quick settings tile
 */
@Composable
fun QsTileRecordActivityDialog(
    typeRows: List<List<ActivityType>>,
    onDismiss: () -> Unit,
    onSave: (TypeRecording) -> Unit
) {
    var state by remember { mutableStateOf(ActivitySelectorState(null, null)) }
    val saveEnabled by remember { derivedStateOf { state.shouldSaveBeEnabled() } }
    Column {
        ActivitySelector(
            state = state,
            typeRows = typeRows,
            onActivityType = { state = state.copy(activityType = it) },
            onVehicle = { state = state.copy(vehicle = it) }
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