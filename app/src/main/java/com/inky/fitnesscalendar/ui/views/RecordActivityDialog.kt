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
import com.inky.fitnesscalendar.db.entities.Recording
import com.inky.fitnesscalendar.db.entities.TypeRecording
import com.inky.fitnesscalendar.di.DecisionTrees
import com.inky.fitnesscalendar.ui.components.ActivitySelector
import com.inky.fitnesscalendar.ui.components.ActivitySelectorState
import com.inky.fitnesscalendar.ui.components.BaseEditDialog
import com.inky.fitnesscalendar.ui.util.localDatabaseValues
import java.time.Instant
import java.util.Date

@Composable
fun RecordActivity(
    onStart: (TypeRecording) -> Unit,
    onNavigateBack: () -> Unit
) {
    var state by remember {
        mutableStateOf(
            ActivitySelectorState(
                activityType = DecisionTrees.activityType?.classifyNow()?.takeIf { it.hasDuration },
                vehicle = DecisionTrees.vehicle?.classifyNow(),
            )
        )
    }

    val typeRows = localDatabaseValues.current.activityTypeRows
    val relevantTypeRows =
        remember(typeRows) { typeRows.map { row -> row.filter { it.hasDuration } } }

    val context = LocalContext.current
    val title = remember(state) {
        when (val type = state.activityType) {
            null -> context.getString(R.string.record_activity)
            else -> context.getString(R.string.record_activity_type, type.name)
        }
    }

    val enabled by remember {
        derivedStateOf {
            state.shouldSaveBeEnabled()
        }
    }

    BaseEditDialog(
        title = title,
        onNavigateBack = onNavigateBack,
        onSave = {
            val type = state.activityType!!
            val recording = Recording(
                typeId = type.uid!!,
                vehicle = state.vehicle,
                startTime = Date.from(Instant.now())
            )
            onStart(TypeRecording(recording = recording, type = type))
        },
        saveEnabled = enabled,
        actions = {},
        saveText = { Text(stringResource(R.string.action_record)) }
    ) {
        ActivitySelector(
            state,
            typeRows = relevantTypeRows,
            onActivityType = { state = state.copy(activityType = it) },
            onVehicle = { state = state.copy(vehicle = it) },
            modifier = Modifier.padding(all = 8.dp)
        )
    }
}