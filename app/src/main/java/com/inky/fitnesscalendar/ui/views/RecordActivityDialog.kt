package com.inky.fitnesscalendar.ui.views

import androidx.compose.foundation.layout.padding
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
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.Recording
import com.inky.fitnesscalendar.db.entities.TypeRecording
import com.inky.fitnesscalendar.di.ActivityTypeDecisionTree
import com.inky.fitnesscalendar.ui.components.ActivitySelector
import com.inky.fitnesscalendar.ui.components.ActivitySelectorState
import com.inky.fitnesscalendar.ui.components.BaseEditDialog
import java.time.Instant
import java.util.Date

@Composable
fun RecordActivity(
    typeRows: List<List<ActivityType>>,
    onStart: (TypeRecording) -> Unit,
    onNavigateBack: () -> Unit
) {
    var activityType by remember { mutableStateOf(ActivityTypeDecisionTree.decisionTree?.classifyNow()) }
    var vehicle by remember { mutableStateOf<Vehicle?>(null) }

    val relevantTypeRows =
        remember(typeRows) { typeRows.map { row -> row.filter { it.hasDuration } } }

    val context = LocalContext.current
    val title = remember(activityType) {
        when (val type = activityType) {
            null -> context.getString(R.string.record_activity)
            else -> context.getString(R.string.record_activity_type, type.name)
        }
    }

    val enabled by remember {
        derivedStateOf {
            activityType != null && (activityType?.hasVehicle != true || vehicle != null)
        }
    }

    BaseEditDialog(
        title = title,
        onNavigateBack = onNavigateBack,
        onSave = {
            val type = activityType!!
            val recording = Recording(
                typeId = type.uid!!,
                vehicle = vehicle,
                startTime = Date.from(Instant.now())
            )
            onStart(TypeRecording(recording = recording, type = type))
        },
        saveEnabled = enabled,
        actions = {},
        saveText = { Text(stringResource(R.string.action_record)) }
    ) {
        ActivitySelector(
            ActivitySelectorState(activityType, vehicle),
            typeRows = relevantTypeRows,
            onActivityType = { activityType = it },
            onVehicle = { vehicle = it },
            modifier = Modifier.padding(all = 8.dp)
        )
    }
}