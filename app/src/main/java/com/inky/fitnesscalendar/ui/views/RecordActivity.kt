package com.inky.fitnesscalendar.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.Recording
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.di.ActivityTypeDecisionTree
import com.inky.fitnesscalendar.ui.components.ActivitySelector
import com.inky.fitnesscalendar.ui.components.ActivitySelectorState
import java.time.Instant
import java.util.Date

@Composable
fun RecordActivity(
    onStart: (Recording) -> Unit,
    onNavigateBack: () -> Unit
) {
    var activityType by remember { mutableStateOf(ActivityTypeDecisionTree.decisionTree?.classifyNow()) }
    var vehicle by remember { mutableStateOf<Vehicle?>(null) }

    val context = LocalContext.current
    val title = remember(activityType) {
        when (val type = activityType) {
            null -> context.getString(R.string.record_activity)
            else -> context.getString(R.string.record_activity_type, context.getString(type.nameId))
        }
    }

    val enabled by remember {
        derivedStateOf {
            activityType != null && (activityType?.hasVehicle != true || vehicle != null)
        }
    }

    Dialog(onDismissRequest = onNavigateBack) {
        Card(
            colors = CardDefaults.cardColors(
                contentColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
                Text(
                    title,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )
                HorizontalDivider()
            }

            ActivitySelector(
                ActivitySelectorState(activityType, vehicle),
                onActivityType = { activityType = it },
                onVehicle = { vehicle = it },
                modifier = Modifier.padding(all = 8.dp)
            )

            Row(modifier = Modifier.align(Alignment.End)) {
                TextButton(onClick = onNavigateBack) {
                    Text(stringResource(R.string.cancel))
                }

                TextButton(enabled = enabled, onClick = {
                    val recording = Recording(
                        type = activityType!!,
                        vehicle = vehicle,
                        startTime = Date.from(Instant.now())
                    )
                    onStart(recording)
                }) {
                    Text(stringResource(R.string.record))
                }
            }
        }
    }
}