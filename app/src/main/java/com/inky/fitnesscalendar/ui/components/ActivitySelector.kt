package com.inky.fitnesscalendar.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.db.entities.ActivityType

data class ActivitySelectorState(val activityType: ActivityType?, val vehicle: Vehicle?) {
    fun shouldSaveBeEnabled() =
        activityType != null && (!activityType.hasVehicle || vehicle != null)
}

@Composable
fun ActivitySelector(
    state: ActivitySelectorState,
    modifier: Modifier = Modifier,
    background: Color = optionGroupDefaultBackground(),
    typeRows: List<List<ActivityType>>,
    onActivityType: (ActivityType) -> Unit,
    onVehicle: (Vehicle) -> Unit,
) {
    val vehicles = remember { Vehicle.entries.toList() }

    Column(modifier = modifier) {
        OptionGroup(
            label = stringResource(R.string.select_activity),
            selectionLabel = state.activityType?.name,
            background = background
        ) {
            ActivityTypeSelector(
                typeRows = typeRows,
                isSelected = { it == state.activityType },
                onSelect = onActivityType
            )
        }

        AnimatedVisibility(state.activityType?.hasVehicle == true) {
            OptionGroup(
                label = stringResource(R.string.select_vehicle),
                selectionLabel = state.vehicle?.nameId?.let { stringResource(it) },
                background = background
            ) {
                LazyRow {
                    items(vehicles) {
                        FilterChip(
                            selected = state.vehicle == it,
                            onClick = { onVehicle(it) },
                            label = {
                                Text(
                                    it.emoji,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}