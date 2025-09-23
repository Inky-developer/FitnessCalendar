package com.inky.fitnesscalendar.ui.components

import android.os.Parcelable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.db.entities.Recording
import com.inky.fitnesscalendar.db.entities.RichRecording
import com.inky.fitnesscalendar.di.DecisionTrees
import com.inky.fitnesscalendar.ui.util.localDatabaseValues
import com.inky.fitnesscalendar.util.Option
import com.inky.fitnesscalendar.util.some
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.util.Date

@Parcelize
data class ActivitySelectorState(
    val selectedActivityType: ActivityType? = null,
    val selectedVehicle: Vehicle? = null,
    val selectedPlace: Option<Place?> = Option.None,
    val activityTypeFilter: ActivityTypeFilter = ActivityTypeFilter.None
) : Parcelable {
    enum class ActivityTypeFilter {
        None,
        RequireDuration;

        fun filter(activityType: ActivityType?) = when (this) {
            None -> activityType
            RequireDuration -> activityType?.takeIf { it.hasDuration }
        }

        @Composable
        fun typeRows() = localDatabaseValues.current.activityTypeRows.map { row ->
            row.mapNotNull { filter(it) }
        }
    }

    @IgnoredOnParcel
    val prediction = DecisionTrees.classifyNow(selectedActivityType)

    @IgnoredOnParcel
    val activityType = activityTypeFilter.filter(selectedActivityType ?: prediction.activityType)

    @IgnoredOnParcel
    val vehicle = selectedVehicle ?: prediction.vehicle

    @IgnoredOnParcel
    val place = selectedPlace.or { prediction.place }

    @IgnoredOnParcel
    val isValid get() = activityType != null && (!activityType.hasVehicle || vehicle != null)

    fun toRecording(): RichRecording? {
        return RichRecording(
            recording = Recording(
                typeId = activityType?.uid ?: return null,
                placeId = place?.uid,
                vehicle = vehicle,
                startTime = Date.from(Instant.now())
            ),
            type = activityType,
            place = place
        )
    }
}

@Composable
fun ActivitySelector(
    state: ActivitySelectorState,
    modifier: Modifier = Modifier,
    background: Color = optionGroupDefaultBackground(),
    onState: (ActivitySelectorState) -> Unit,
    onNavigateNewPlace: (() -> Unit)? = null,
) {
    val vehicles = remember { Vehicle.entries.toList() }
    val typeRows = state.activityTypeFilter.typeRows()

    Column(modifier = modifier) {
        OptionGroup(
            label = stringResource(R.string.select_activity),
            selectionLabel = state.activityType?.name,
            background = background
        ) {
            ActivityTypeSelector(
                typeRows = typeRows,
                isSelected = { it == state.activityType },
                onSelect = { onState(state.copy(selectedActivityType = it)) }
            )
        }

        AnimatedVisibility(visible = state.activityType?.hasPlace == true && localDatabaseValues.current.places.isNotEmpty()) {
            PlaceSelector(
                currentPlace = state.place,
                onPlace = { onState(state.copy(selectedPlace = it.some())) },
                onNavigateNewPlace = onNavigateNewPlace,
                placeFilter = { state.activityType?.limitPlacesByColor == null || state.activityType.limitPlacesByColor == it.color }
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
                            onClick = { onState(state.copy(selectedVehicle = it)) },
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

@Composable
private fun PlaceSelector(
    currentPlace: Place?,
    onPlace: (Place?) -> Unit,
    onNavigateNewPlace: (() -> Unit)?,
    placeFilter: (Place) -> Boolean
) {
    val allPlaces = localDatabaseValues.current.places
    val places = remember(placeFilter, allPlaces) { allPlaces.filter(placeFilter) }
    var showDialog by rememberSaveable { mutableStateOf(false) }

    TextButton(
        onClick = { showDialog = true },
        colors = ButtonDefaults.textButtonColors(containerColor = optionGroupDefaultBackground()),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedContent(
            targetState = currentPlace,
            label = stringResource(R.string.place)
        ) { place ->
            if (place != null) {
                PlaceInfo(place)
            } else {
                Text(stringResource(R.string.select_place))
            }
        }
    }

    DropdownMenu(
        expanded = showDialog,
        onDismissRequest = { showDialog = false },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp)
    ) {
        if (currentPlace != null) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.no_place)) },
                leadingIcon = { Icon(Icons.Outlined.Clear, stringResource(R.string.no_place)) },
                onClick = {
                    showDialog = false
                    onPlace(null)
                },
            )
        } else if (onNavigateNewPlace != null) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.new_place)) },
                leadingIcon = { Icon(Icons.Outlined.Add, stringResource(R.string.new_place)) },
                onClick = onNavigateNewPlace,
            )
        }

        for (place in places) {
            DropdownMenuItem(
                text = { Text(place.name) },
                leadingIcon = { PlaceIcon(place) },
                onClick = {
                    showDialog = false
                    onPlace(place)
                },
            )
        }
    }
}