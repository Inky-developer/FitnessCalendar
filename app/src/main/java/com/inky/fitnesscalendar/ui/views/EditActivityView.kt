package com.inky.fitnesscalendar.ui.views

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.di.DecisionTrees
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.components.ActivitySelector
import com.inky.fitnesscalendar.ui.components.ActivitySelectorState
import com.inky.fitnesscalendar.ui.components.DateTimePicker
import com.inky.fitnesscalendar.ui.components.FeelSelector
import com.inky.fitnesscalendar.ui.components.ImageViewer
import com.inky.fitnesscalendar.ui.components.OptionGroup
import com.inky.fitnesscalendar.ui.components.optionGroupDefaultBackground
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
    onSave: (RichActivity) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateNewPlace: () -> Unit
) {
    val activity =
        (activityId?.let { viewModel.repository.getActivity(it) } ?: flowOf(null)).collectAsState(
            initial = null
        )


    if (activityId == null || activity.value != null) {
        NewActivity(
            richActivity = activity.value,
            localizationRepository = viewModel.localizationRepository,
            onSave = onSave,
            onNavigateBack = onNavigateBack,
            onNavigateNewPlace = onNavigateNewPlace,
        )
    } else {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewActivity(
    richActivity: RichActivity?,
    localizationRepository: LocalizationRepository,
    onSave: (RichActivity) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateNewPlace: () -> Unit,
) {
    val context = LocalContext.current
    val activityPrediction = remember { DecisionTrees.classifyNow(context) }

    val title = richActivity?.type?.let { stringResource(R.string.edit_object, it.name) }
        ?: stringResource(R.string.new_activity)

    var selectedActivityType by rememberSaveable {
        mutableStateOf(richActivity?.type ?: activityPrediction.activityType)
    }
    var selectedVehicle by rememberSaveable {
        mutableStateOf(richActivity?.activity?.vehicle ?: activityPrediction.vehicle)
    }
    var selectedPlace by rememberSaveable {
        mutableStateOf(richActivity?.place ?: activityPrediction.place)
    }
    var startDateTime by rememberSaveable {
        mutableStateOf(richActivity?.activity?.startTime?.toLocalDateTime() ?: LocalDateTime.now())
    }
    var endDateTime by rememberSaveable {
        mutableStateOf(
            richActivity?.activity?.endTime?.toLocalDateTime() ?: startDateTime.plusHours(1)
        )
    }
    var description by rememberSaveable {
        mutableStateOf(richActivity?.activity?.description ?: "")
    }

    var distanceString by rememberSaveable {
        mutableStateOf(
            richActivity?.activity?.distance?.kilometers?.toString() ?: ""
        )
    }
    val isDistanceStringError by remember {
        derivedStateOf {
            distanceString.isNotBlank() && kilometerStringToDistance(distanceString) == null
        }
    }

    var intensity by rememberSaveable {
        mutableStateOf(richActivity?.activity?.intensity?.value)
    }

    var feel by rememberSaveable { mutableStateOf(richActivity?.activity?.feel) }

    var imageUri by rememberSaveable { mutableStateOf(richActivity?.activity?.imageUri) }

    val formValid = selectedActivityType != null
            && (!selectedActivityType!!.hasVehicle || selectedVehicle != null)
            && !isDistanceStringError
            && !endDateTime.isBefore(startDateTime)

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

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val topAppBarColors = TopAppBarDefaults.topAppBarColors(
        scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                colors = topAppBarColors,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
                    }
                },
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
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible = formValid, enter = fadeIn(), exit = fadeOut()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val oldActivity = when (richActivity) {
                            null -> Activity(
                                typeId = 0,
                                startTime = startDateTime.toDate()
                            )

                            else -> richActivity.activity
                        }
                        val newActivity = oldActivity.copy(
                            typeId = selectedActivityType?.uid!!,
                            placeId = selectedPlace?.uid,
                            vehicle = selectedVehicle,
                            description = description,
                            startTime = startDateTime.toDate(),
                            endTime = endDateTime.toDate(),
                            feel = feel,
                            imageUri = imageUri,
                            distance = kilometerStringToDistance(distanceString),
                            intensity = intensity?.let { Intensity(it) }
                        )

                        onSave(
                            RichActivity(
                                activity = newActivity,
                                place = selectedPlace,
                                type = selectedActivityType!!
                            )
                        )
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Done, stringResource(R.string.save))
                        Text(
                            stringResource(R.string.save),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
                .verticalScroll(scrollState)
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri!!,
                    contentDescription = stringResource(R.string.user_uploaded_image),
                    onState = { state ->
                        if (state is AsyncImagePainter.State.Error) {
                            imageUri = richActivity?.activity?.imageUri
                        }
                    },
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth()
                        .heightIn(max = 256.dp)
                        .clip(MaterialTheme.shapes.large)
                        .clickable {
                            showImageViewer = true
                        }
                )
            }

            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth()
            ) {
                DateTimeInput(
                    dateTime = startDateTime,
                    localizationRepository = localizationRepository,
                    labelId = R.string.datetime_start,
                    onDateTime = { startDateTime = it },
                )

                AnimatedVisibility(visible = selectedActivityType?.hasDuration == true) {
                    DateTimeInput(
                        dateTime = endDateTime,
                        localizationRepository = localizationRepository,
                        labelId = R.string.datetime_end,
                        onDateTime = { endDateTime = it },
                        isError = endDateTime.isBefore(startDateTime),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            ActivitySelector(
                ActivitySelectorState(selectedActivityType, selectedVehicle, selectedPlace),
                onState = {
                    selectedActivityType = it.activityType
                    selectedVehicle = it.vehicle
                    selectedPlace = it.place
                },
                onNavigateNewPlace = onNavigateNewPlace
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
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = optionGroupDefaultBackground()),
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
                keyboardOptions = remember { KeyboardOptions(capitalization = KeyboardCapitalization.Sentences) },
                colors = TextFieldDefaults.colors(unfocusedContainerColor = optionGroupDefaultBackground()),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            )

            Spacer(modifier = Modifier.height(128.dp))
        }

        if (showImageViewer && imageUri != null) {
            ImageViewer(
                imageUri!!,
                onDismiss = { showImageViewer = false },
                onDelete = {
                    imageUri = null
                    showImageViewer = false
                },
            )
        }
    }
}

@Composable
private fun RowScope.DateTimeInput(
    dateTime: LocalDateTime,
    localizationRepository: LocalizationRepository,
    labelId: Int,
    onDateTime: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    val dateTimeStr = remember(dateTime) {
        val date = dateTime.toDate()
        val startDateStr = localizationRepository.dateFormatter.format(date.time)
        val startTimeStr = localizationRepository.timeFormatter.format(date)

        startDateStr to startTimeStr
    }
    val errorColor = MaterialTheme.colorScheme.error
    val border = remember(isError) {
        if (isError) {
            BorderStroke(width = 1.dp, color = errorColor)
        } else {
            null
        }
    }

    var showPicker by rememberSaveable { mutableStateOf(false) }

    TextButton(
        onClick = { showPicker = true },
        colors = ButtonDefaults.textButtonColors(
            containerColor = optionGroupDefaultBackground(),
            contentColor = contentColorFor(optionGroupDefaultBackground())
        ),
        border = border,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.weight(1f)
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