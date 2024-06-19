package com.inky.fitnesscalendar.ui.views

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.Activity
import com.inky.fitnesscalendar.data.TypeActivity
import com.inky.fitnesscalendar.di.ActivityTypeDecisionTree
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.components.ActivitySelector
import com.inky.fitnesscalendar.ui.components.ActivitySelectorState
import com.inky.fitnesscalendar.ui.components.DateTimePicker
import com.inky.fitnesscalendar.ui.components.DateTimePickerState
import com.inky.fitnesscalendar.ui.components.FeelSelector
import com.inky.fitnesscalendar.ui.components.ImageViewer
import com.inky.fitnesscalendar.ui.components.OptionGroup
import com.inky.fitnesscalendar.util.copyFileToStorage
import com.inky.fitnesscalendar.util.getOrCreateActivityImagesDir
import com.inky.fitnesscalendar.view_model.NewActivityViewModel
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

@Composable
fun NewActivity(
    activityId: Int?,
    viewModel: NewActivityViewModel = hiltViewModel(),
    onSave: (TypeActivity) -> Unit,
    onNavigateBack: () -> Unit
) {
    val activity =
        (activityId?.let { viewModel.repository.getActivity(it) } ?: flowOf(null)).collectAsState(
            initial = null
        )


    if (activityId == null) {
        NewActivity(
            typeActivity = null,
            localizationRepository = viewModel.localizationRepository,
            onSave = onSave,
            onNavigateBack = onNavigateBack
        )
    } else if (activity.value != null) {
        NewActivity(
            typeActivity = activity.value,
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
    typeActivity: TypeActivity?,
    localizationRepository: LocalizationRepository,
    onSave: (TypeActivity) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val title = typeActivity?.type?.let { stringResource(R.string.edit_activity, it.name) }
        ?: stringResource(R.string.new_activity)

    var selectedActivityType by rememberSaveable {
        mutableStateOf(
            typeActivity?.type ?: ActivityTypeDecisionTree.decisionTree?.classifyNow()
        )
    }
    var selectedVehicle by rememberSaveable { mutableStateOf(typeActivity?.activity?.vehicle) }
    val startDateTimePickerState by rememberSaveable(stateSaver = DateTimePickerState.SAVER) {
        mutableStateOf(
            DateTimePickerState(
                initialDateTime = typeActivity?.activity?.startTime?.time ?: Instant.now()
                    .toEpochMilli()
            )
        )
    }
    val endDateTimePickerState by rememberSaveable(stateSaver = DateTimePickerState.SAVER) {
        mutableStateOf(
            DateTimePickerState(
                initialDateTime = typeActivity?.activity?.endTime?.time
                    ?: startDateTimePickerState.selectedDateTime
            )
        )
    }
    var description by rememberSaveable {
        mutableStateOf(typeActivity?.activity?.description ?: "")
    }

    var feel by rememberSaveable { mutableStateOf(typeActivity?.activity?.feel) }

    var imageUri by rememberSaveable { mutableStateOf(typeActivity?.activity?.imageUri) }

    val formValid =
        selectedActivityType != null && (!selectedActivityType!!.hasVehicle || selectedVehicle != null)

    val scrollState = rememberScrollState()

    var contextMenuOpen by rememberSaveable { mutableStateOf(false) }
    var showImageViewer by rememberSaveable { mutableStateOf(false) }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
            val actualUri = uri?.let {
                context.copyFileToStorage(it, context.getOrCreateActivityImagesDir())
            }
            if (actualUri != null) {
                imageUri = actualUri
            }
        }

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Column {
                        IconButton(onClick = { contextMenuOpen = true }) {
                            Icon(Icons.Outlined.Menu, stringResource(R.string.open_context_menu))
                        }
                        DropdownMenu(
                            expanded = contextMenuOpen,
                            onDismissRequest = { contextMenuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.add_image)) },
                                leadingIcon = {
                                    Icon(
                                        painterResource(R.drawable.outline_add_image_24),
                                        stringResource(R.string.add_image)
                                    )
                                },
                                onClick = {
                                    contextMenuOpen = false
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                        }
                    }
                }
                HorizontalDivider()
            }

            Column(
                modifier = Modifier
                    .padding(all = 8.dp)
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState)
            ) {
                if (imageUri != null) {
                    Column {
                        AsyncImage(
                            model = imageUri!!,
                            contentDescription = stringResource(R.string.user_uploaded_image),
                            onState = { state ->
                                if (state is AsyncImagePainter.State.Error) {
                                    imageUri = typeActivity?.activity?.imageUri
                                }
                            },
                            contentScale = ContentScale.FillHeight,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .clip(MaterialTheme.shapes.large)
                                .clickable {
                                    showImageViewer = true
                                }
                        )
                    }
                }

                ActivitySelector(
                    ActivitySelectorState(selectedActivityType, selectedVehicle),
                    onActivityType = { selectedActivityType = it },
                    onVehicle = { selectedVehicle = it }
                )

                AnimatedVisibility(visible = selectedActivityType?.hasFeel() == true) {
                    OptionGroup(
                        label = stringResource(R.string.select_feel),
                        selectionLabel = feel?.let { stringResource(it.nameId) }) {
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
                        val oldActivity = when (typeActivity) {
                            null -> Activity(
                                typeId = 0,
                                startTime = startDateTimePickerState.selectedDate()
                            )

                            else -> typeActivity.activity
                        }
                        val newActivity = oldActivity.copy(
                            typeId = selectedActivityType?.uid!!,
                            vehicle = selectedVehicle,
                            description = description,
                            startTime = startDateTimePickerState.selectedDate(),
                            endTime = endDateTimePickerState.selectedDate(),
                            feel = feel,
                            imageUri = imageUri
                        )

                        onSave(TypeActivity(activity = newActivity, type = selectedActivityType!!))
                    },
                    enabled = formValid
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }

    if (showImageViewer && imageUri != null) {
        ImageViewer(
            imageUri!!,
            onDismiss = { showImageViewer = false },
            onDelete = {
                imageUri = null
                showImageViewer = false
            }
        )
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