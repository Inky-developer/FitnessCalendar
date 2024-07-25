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
import com.inky.fitnesscalendar.db.entities.RichRecording
import com.inky.fitnesscalendar.ui.components.ActivitySelector
import com.inky.fitnesscalendar.ui.components.ActivitySelectorState
import com.inky.fitnesscalendar.ui.components.BaseEditDialog
import com.inky.fitnesscalendar.ui.util.localDatabaseValues

@Composable
fun RecordActivity(
    onStart: (RichRecording) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var state by remember {
        mutableStateOf(ActivitySelectorState.fromPrediction(context, requireTypeHasDuration = true))
    }

    val typeRows = localDatabaseValues.current.activityTypeRows
    val relevantTypeRows =
        remember(typeRows) { typeRows.map { row -> row.filter { it.hasDuration } } }

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
        onSave = { onStart(state.toRecording()!!) },
        saveEnabled = enabled,
        actions = {},
        saveText = { Text(stringResource(R.string.action_record)) }
    ) {
        ActivitySelector(
            state,
            typeRows = relevantTypeRows,
            modifier = Modifier.padding(all = 8.dp),
            onState = { state = it }
        )
    }
}