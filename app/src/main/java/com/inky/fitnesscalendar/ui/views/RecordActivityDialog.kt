package com.inky.fitnesscalendar.ui.views

import android.content.Context
import android.os.Parcelable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.RichRecording
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.components.ActivitySelector
import com.inky.fitnesscalendar.ui.components.ActivitySelectorState
import com.inky.fitnesscalendar.ui.components.BaseEditDialog
import com.inky.fitnesscalendar.ui.components.DateTimePicker
import com.inky.fitnesscalendar.ui.util.localDatabaseValues
import com.inky.fitnesscalendar.util.toDate
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Composable
fun RecordActivity(
    onStart: (RichRecording) -> Unit,
    localizationRepository: LocalizationRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var state by rememberSaveable {
        mutableStateOf(RecordActivityState.fromPrediction(context))
    }

    val typeRows = localDatabaseValues.current.activityTypeRows
    val relevantTypeRows =
        remember(typeRows) { typeRows.map { row -> row.filter { it.hasDuration } } }

    BaseEditDialog(
        title = state.title,
        onNavigateBack = onNavigateBack,
        onSave = { onStart(state.toRecording()!!) },
        saveEnabled = state.isValid,
        actions = {},
        saveText = { Text(stringResource(R.string.action_record)) }
    ) {
        RecordActivityInner(
            state = state,
            typeRows = relevantTypeRows,
            localizationRepository = localizationRepository,
            onState = { state = it },
            includeTimePicker = true,
            modifier = Modifier.padding(all = 8.dp)
        )
    }
}

@Composable
fun RecordActivityInner(
    state: RecordActivityState,
    typeRows: List<List<ActivityType>>,
    localizationRepository: LocalizationRepository,
    onState: (RecordActivityState) -> Unit,
    includeTimePicker: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        ActivitySelector(
            state = state.activitySelectorState,
            typeRows = typeRows,
            onState = { onState(state.copy(activitySelectorState = it)) }
        )

        if (includeTimePicker) {
            // Currently it is possible to select dates in the future
            // This might be a feature though: If I know exactly when an activity will start, I can
            // use this to configure the recording in advance.
            DateTimeInput(
                date = state.customStart,
                localizationRepository = localizationRepository,
                onDate = { onState(state.copy(customStart = it)) }
            )
        }
    }
}

@Composable
private fun DateTimeInput(
    date: LocalDateTime?,
    localizationRepository: LocalizationRepository,
    onDate: (LocalDateTime) -> Unit
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }

    TextButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
        val text = when (date) {
            null -> stringResource(R.string.select_custom_start_time)
            else -> stringResource(
                R.string.start_time,
                localizationRepository.formatRelativeDate(date.toDate())
            )
        }
        Text(text)
    }

    if (showPicker) {
        DateTimePicker(
            onDismiss = { showPicker = false },
            onOkay = {
                showPicker = false
                onDate(it)
            }
        )
    }
}

@Parcelize
data class RecordActivityState(
    val activitySelectorState: ActivitySelectorState,
    val customStart: LocalDateTime?,
) : Parcelable {
    @IgnoredOnParcel
    val title: String
        @Composable get() = when (val type = activitySelectorState.activityType) {
            null -> stringResource(R.string.record_activity)
            else -> stringResource(R.string.record_activity_type, type.name)
        }

    @IgnoredOnParcel
    val isValid get() = activitySelectorState.isValid

    fun toRecording(): RichRecording? {
        val rawRecording = activitySelectorState.toRecording() ?: return null
        return if (customStart != null) {
            rawRecording.copy(recording = rawRecording.recording.copy(startTime = customStart.toDate()))
        } else {
            rawRecording
        }
    }

    companion object {
        fun fromPrediction(context: Context) = RecordActivityState(
            activitySelectorState = ActivitySelectorState.fromPrediction(
                context,
                requireTypeHasDuration = true
            ),
            customStart = null
        )
    }
}