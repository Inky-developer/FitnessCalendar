package com.inky.fitnesscalendar.ui.views

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.Intensity
import com.inky.fitnesscalendar.data.measure.Distance
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.TypeActivity
import com.inky.fitnesscalendar.di.DecisionTrees
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.components.ActivitySelector
import com.inky.fitnesscalendar.ui.components.ActivitySelectorState
import com.inky.fitnesscalendar.ui.components.BaseEditDialog
import com.inky.fitnesscalendar.ui.components.DateTimePicker
import com.inky.fitnesscalendar.ui.components.FeelSelector
import com.inky.fitnesscalendar.ui.components.ImageViewer
import com.inky.fitnesscalendar.ui.components.OptionGroup
import com.inky.fitnesscalendar.util.copyFileToStorage
import com.inky.fitnesscalendar.util.getOrCreateActivityImagesDir
import com.inky.fitnesscalendar.util.toDate
import com.inky.fitnesscalendar.util.toLocalDateTime
import com.inky.fitnesscalendar.view_model.NewActivityViewModel
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime
import kotlin.math.roundToInt

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


    if (activityId == null || activity.value != null) {
        NewActivity(
            typeActivity = activity.value,
            localizationRepository = viewModel.localizationRepository,
            onSave = onSave,
            onNavigateBack = onNavigateBack,
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
            typeActivity?.type ?: DecisionTrees.activityType?.classifyNow()
        )
    }
    var selectedVehicle by rememberSaveable {
        mutableStateOf(
            typeActivity?.activity?.vehicle ?: DecisionTrees.vehicle?.classifyNow()
        )
    }
    var startDateTime by rememberSaveable {
        mutableStateOf(typeActivity?.activity?.startTime?.toLocalDateTime() ?: LocalDateTime.now())
    }
    var endDateTime by rememberSaveable {
        mutableStateOf(
            typeActivity?.activity?.endTime?.toLocalDateTime() ?: startDateTime.plusHours(1)
        )
    }
    var description by rememberSaveable {
        mutableStateOf(typeActivity?.activity?.description ?: "")
    }

    var distanceString by rememberSaveable {
        mutableStateOf(
            typeActivity?.activity?.distance?.kilometers?.toString() ?: ""
        )
    }
    val isDistanceStringError by remember {
        derivedStateOf {
            distanceString.isNotBlank() && kilometerStringToDistance(distanceString) == null
        }
    }

    var intensity by rememberSaveable {
        mutableStateOf(typeActivity?.activity?.intensity?.value)
    }

    var feel by rememberSaveable { mutableStateOf(typeActivity?.activity?.feel) }

    var imageUri by rememberSaveable { mutableStateOf(typeActivity?.activity?.imageUri) }

    val formValid =
        selectedActivityType != null && (!selectedActivityType!!.hasVehicle || selectedVehicle != null) && !isDistanceStringError

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

    BaseEditDialog(
        saveEnabled = formValid,
        onNavigateBack = onNavigateBack,
        onSave = {
            val oldActivity = when (typeActivity) {
                null -> Activity(
                    typeId = 0,
                    startTime = startDateTime.toDate()
                )

                else -> typeActivity.activity
            }
            val newActivity = oldActivity.copy(
                typeId = selectedActivityType?.uid!!,
                vehicle = selectedVehicle,
                description = description,
                startTime = startDateTime.toDate(),
                endTime = endDateTime.toDate(),
                feel = feel,
                imageUri = imageUri,
                distance = kilometerStringToDistance(distanceString),
                intensity = intensity?.let { Intensity(it) }
            )

            onSave(TypeActivity(activity = newActivity, type = selectedActivityType!!))
        },
        title = title,
        actions = {
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
    ) {
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
                onVehicle = { selectedVehicle = it },
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

            AnimatedVisibility(visible = selectedActivityType?.hasDistance == true) {
                TextField(
                    value = distanceString,
                    onValueChange = { distanceString = it },
                    isError = isDistanceStringError,
                    placeholder = { Text(stringResource(R.string.placeholder_distance)) },
                    suffix = { Text(stringResource(R.string.km)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    singleLine = true,
                    keyboardOptions = remember { KeyboardOptions(keyboardType = KeyboardType.Number) },
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shape = MaterialTheme.shapes.small
                )
            }

            AnimatedVisibility(visible = selectedActivityType?.hasIntensity == true) {
                OptionGroup(
                    label = stringResource(R.string.select_intensity),
                    selectionLabel = intensity?.toString()
                ) {
                    Slider(
                        value = intensity?.toFloat() ?: -1f,
                        onValueChange = {
                            intensity = if (it < 0) {
                                null
                            } else {
                                it.roundToInt().toByte()
                            }
                        },
                        steps = 10,
                        valueRange = -1f..10f
                    )
                }
            }

            TextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text(stringResource(R.string.placeholder_description)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                maxLines = 8,
                keyboardOptions = remember { KeyboardOptions(capitalization = KeyboardCapitalization.Sentences) },
                colors = TextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = MaterialTheme.shapes.small
            )

            DateTimeInput(
                dateTime = startDateTime,
                localizationRepository = localizationRepository,
                labelId = R.string.datetime_start,
                onDateTime = { startDateTime = it }
            )

            AnimatedVisibility(visible = selectedActivityType?.hasDuration == true) {
                DateTimeInput(
                    dateTime = endDateTime,
                    localizationRepository = localizationRepository,
                    labelId = R.string.datetime_end,
                    onDateTime = { endDateTime = it }
                )
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
private fun ColumnScope.DateTimeInput(
    dateTime: LocalDateTime,
    localizationRepository: LocalizationRepository,
    labelId: Int,
    onDateTime: (LocalDateTime) -> Unit
) {
    val dateTimeStr = remember(dateTime) {
        val date = dateTime.toDate()
        val startDateStr = localizationRepository.dateFormatter.format(date.time)
        val startTimeStr = localizationRepository.timeFormatter.format(date)

        startDateStr to startTimeStr
    }

    var showPicker by rememberSaveable { mutableStateOf(false) }

    TextButton(
        onClick = { showPicker = true },
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

    if (showPicker) {
        DateTimePicker(
            initialDateTime = dateTime,
            onDismiss = { showPicker = false },
            onOkay = {
                showPicker = false
                onDateTime(it)
            }
        )
    }
}

private fun kilometerStringToDistance(string: String) = if (string.isBlank()) {
    null
} else {
    string.replace(",", ".").toDoubleOrNull()?.let { Distance(kilometers = it) }
}