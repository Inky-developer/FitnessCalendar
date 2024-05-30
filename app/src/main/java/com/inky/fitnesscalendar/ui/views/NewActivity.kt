package com.inky.fitnesscalendar.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.Activity
import com.inky.fitnesscalendar.data.ActivityType
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.di.ActivityTypeDecisionTree
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.components.ActivitySelector
import com.inky.fitnesscalendar.ui.components.ActivitySelectorState
import com.inky.fitnesscalendar.ui.components.DateTimePicker
import com.inky.fitnesscalendar.ui.components.DateTimePickerState
import com.inky.fitnesscalendar.ui.components.FeelSelector
import com.inky.fitnesscalendar.ui.components.OptionGroup
import com.inky.fitnesscalendar.view_model.NewActivityViewModel
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

@Composable
fun NewActivity(
    activityId: Int?,
    viewModel: NewActivityViewModel = hiltViewModel(),
    onSave: (Activity) -> Unit,
    onNavigateBack: () -> Unit
) {
    val activity =
        (activityId?.let { viewModel.repository.getActivity(it) } ?: flowOf(null)).collectAsState(
            initial = null
        )


    if (activityId == null) {
        NewActivity(
            activity = null,
            localizationRepository = viewModel.localizationRepository,
            onSave = onSave,
            onNavigateBack = onNavigateBack
        )
    } else if (activity.value != null) {
        NewActivity(
            activity = activity.value,
            localizationRepository = viewModel.localizationRepository,
            onSave = onSave,
            onNavigateBack = onNavigateBack
        )
    } else {
        CircularProgressIndicator()
    }
}

@Composable
fun NewActivity(
    activity: Activity?,
    localizationRepository: LocalizationRepository,
    onSave: (Activity) -> Unit,
    onNavigateBack: () -> Unit
) {
    val title = activity?.type?.let {
        stringResource(
            R.string.edit_activity,
            stringResource(it.nameId)
        )
    } ?: stringResource(R.string.new_activity)

    var selectedActivityType by rememberSaveable {
        mutableStateOf(
            activity?.type ?: ActivityTypeDecisionTree.decisionTree?.classifyNow()
        )
    }
    var selectedVehicle by rememberSaveable { mutableStateOf(activity?.vehicle) }
    val startDateTimePickerState by rememberSaveable(stateSaver = DateTimePickerState.SAVER) {
        mutableStateOf(
            DateTimePickerState(
                initialDateTime = activity?.startTime?.time ?: Instant.now().toEpochMilli()
            )
        )
    }
    val endDateTimePickerState by rememberSaveable(stateSaver = DateTimePickerState.SAVER) {
        mutableStateOf(
            DateTimePickerState(
                initialDateTime = activity?.endTime?.time
                    ?: startDateTimePickerState.selectedDateTime
            )
        )
    }
    var description by rememberSaveable { mutableStateOf(activity?.description ?: "") }

    var feel by rememberSaveable { mutableStateOf<Feel?>(activity?.feel) }

    val scrollState = rememberScrollState()

    val formValid =
        selectedActivityType != null && (!selectedActivityType!!.hasVehicle || selectedVehicle != null)

    // When the start date time changes, move the end date time along as well
    LaunchedEffect(startDateTimePickerState.selectedDateTime) {
        if (endDateTimePickerState.selectedDateTime == endDateTimePickerState.initialDateTime && startDateTimePickerState.selectedDateTime != startDateTimePickerState.initialDateTime) {
            endDateTimePickerState.initialDateTime =
                startDateTimePickerState.selectedDateTime + ChronoUnit.HOURS.duration.toMillis()
        }
    }

    Dialog(onDismissRequest = onNavigateBack) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
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

            Column(
                modifier = Modifier
                    .padding(all = 8.dp)
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState)
            ) {
                ActivitySelector(
                    ActivitySelectorState(selectedActivityType, selectedVehicle),
                    onActivityType = { selectedActivityType = it },
                    onVehicle = { selectedVehicle = it }
                )

                AnimatedVisibility(visible = selectedActivityType?.hasFeel() == true) {
                    OptionGroup(label = stringResource(R.string.select_feel)) {
                        FeelSelector(
                            feel = feel,
                            onChange = { feel = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text(stringResource(R.string.placeholder_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shape = MaterialTheme.shapes.small
                )

                DateTimeInput(
                    state = startDateTimePickerState,
                    localizationRepository = localizationRepository,
                    labelId = R.string.datetime_start
                )

                AnimatedVisibility(visible = selectedActivityType?.hasDuration == true) {
                    DateTimeInput(
                        state = endDateTimePickerState,
                        localizationRepository = localizationRepository,
                        labelId = R.string.datetime_end
                    )

                }
            }

            Row(modifier = Modifier.align(Alignment.End)) {
                TextButton(onClick = onNavigateBack) {
                    Text(stringResource(R.string.cancel))
                }
                TextButton(
                    onClick = {
                        val newActivity = when (activity) {
                            null -> Activity(
                                type = selectedActivityType!!,
                                vehicle = selectedVehicle,
                                description = description,
                                startTime = startDateTimePickerState.selectedDate(),
                                endTime = endDateTimePickerState.selectedDate(),
                                feel = feel,
                            )

                            else -> activity.copy(
                                type = selectedActivityType!!,
                                vehicle = selectedVehicle,
                                description = description,
                                startTime = startDateTimePickerState.selectedDate(),
                                endTime = endDateTimePickerState.selectedDate(),
                                feel = feel
                            )
                        }
                        onSave(newActivity)
                    },
                    enabled = formValid
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

@Composable
fun ColumnScope.DateTimeInput(
    state: DateTimePickerState,
    localizationRepository: LocalizationRepository,
    labelId: Int
) {
    val dateTimeStr by remember {
        derivedStateOf {
            val date = Date.from(Instant.ofEpochMilli(state.selectedDateTime))
            val startDateStr = localizationRepository.dateFormatter.format(date.time)
            val startTimeStr = localizationRepository.timeFormatter.format(date)

            startDateStr to startTimeStr
        }
    }


    DateTimePicker(state = state) {
        TextButton(
            onClick = { state.open() },
            colors = ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer)
            ),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(top = 4.dp),
        ) {
            Text(
                stringResource(
                    id = labelId,
                    dateTimeStr.first,
                    dateTimeStr.second
                )
            )
        }
    }
}

@Preview
@Composable
fun NewActivityPreview() {
    val context = LocalContext.current
    val activity = Activity(type = ActivityType.Bouldering, startTime = Date.from(Instant.now()))
    NewActivity(
        activity = activity,
        localizationRepository = LocalizationRepository(context),
        onSave = {},
        onNavigateBack = {})
}