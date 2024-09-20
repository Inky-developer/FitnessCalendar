package com.inky.fitnesscalendar.ui.views

import android.os.Parcelable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImagePainter
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.data.ImageName
import com.inky.fitnesscalendar.data.Intensity
import com.inky.fitnesscalendar.data.measure.Distance
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.di.DecisionTrees
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.components.ActivityImage
import com.inky.fitnesscalendar.ui.components.ActivitySelector
import com.inky.fitnesscalendar.ui.components.ActivitySelectorState
import com.inky.fitnesscalendar.ui.components.DateTimePicker
import com.inky.fitnesscalendar.ui.components.FavoriteIcon
import com.inky.fitnesscalendar.ui.components.FeelSelector
import com.inky.fitnesscalendar.ui.components.ImageViewer
import com.inky.fitnesscalendar.ui.components.OptionGroup
import com.inky.fitnesscalendar.ui.components.SelectImageDropdownMenuItem
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.components.optionGroupDefaultBackground
import com.inky.fitnesscalendar.ui.components.rememberImagePickerLauncher
import com.inky.fitnesscalendar.util.toDate
import com.inky.fitnesscalendar.util.toLocalDateTime
import com.inky.fitnesscalendar.view_model.NewActivityViewModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    val title = remember(richActivity) {
        richActivity?.type?.let { context.getString(R.string.edit_object, it.name) }
            ?: context.getString(R.string.new_activity)
    }
    val initialState = remember { ActivityEditState(richActivity, activityPrediction) }

    var editState by rememberSaveable { mutableStateOf(initialState) }
    val scrollState = rememberScrollState()
    var contextMenuOpen by rememberSaveable { mutableStateOf(false) }
    var showImageViewer by rememberSaveable { mutableStateOf(false) }

    val isKeyboardVisible = WindowInsets.isImeVisible
    val showSaveButton by remember(isKeyboardVisible) { derivedStateOf { !isKeyboardVisible && (richActivity == null || editState != initialState) && editState.isValid } }
    val imagePickerLauncher = rememberImagePickerLauncher(onName = {
        editState = editState.copy(imageName = it)
    })

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                colors = defaultTopAppBarColors(),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    IconToggleButton(
                        checked = editState.favorite,
                        onCheckedChange = { editState = editState.copy(favorite = it) }
                    ) {
                        AnimatedContent(editState.favorite, label = "Favorite") { isFavorite ->
                            FavoriteIcon(isFavorite)
                        }
                    }
                    IconButton(onClick = { contextMenuOpen = true }) {
                        Icon(Icons.Outlined.Menu, stringResource(R.string.open_context_menu))
                    }
                    DropdownMenu(
                        expanded = contextMenuOpen,
                        onDismissRequest = { contextMenuOpen = false }) {
                        SelectImageDropdownMenuItem(
                            imagePickerLauncher = imagePickerLauncher,
                            onDismissMenu = { contextMenuOpen = false },
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible = showSaveButton, enter = fadeIn(), exit = fadeOut()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (editState.isValid) {
                            onSave(editState.toActivity(richActivity))
                        }
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
            val imageUri = editState.imageName?.getImageUri()
            if (imageUri != null) {
                ActivityImage(
                    uri = imageUri,
                    onState = { state ->
                        if (state is AsyncImagePainter.State.Error) {
                            editState =
                                editState.copy(imageName = richActivity?.activity?.imageName)
                        }
                    },
                    onClick = {
                        showImageViewer = true
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            AnimatedContent(
                targetState = editState.activitySelectorState.activityType?.hasDuration == true,
                label = stringResource(R.string.select_date)
            ) { hasDuration ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    val dateLabelId = if (hasDuration) {
                        R.string.datetime_start
                    } else {
                        R.string.datetime_date
                    }
                    DateTimeInput(
                        dateTime = editState.startDateTime,
                        localizationRepository = localizationRepository,
                        labelId = dateLabelId,
                        onDateTime = { editState = editState.copy(startDateTime = it) },
                        modifier = Modifier.weight(1f)
                    )

                    if (hasDuration) {
                        DateTimeInput(
                            dateTime = editState.endDateTime,
                            localizationRepository = localizationRepository,
                            labelId = R.string.datetime_end,
                            onDateTime = { editState = editState.copy(endDateTime = it) },
                            isError = editState.isEndDateTimeError,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .weight(1f)
                        )
                    }
                }
            }

            ActivitySelector(
                editState.activitySelectorState,
                onState = { editState = editState.copy(activitySelectorState = it) },
                onNavigateNewPlace = onNavigateNewPlace
            )

            AnimatedVisibility(visible = editState.activitySelectorState.activityType?.hasFeel() == true) {
                OptionGroup(
                    label = stringResource(R.string.select_feel),
                    selectionLabel = stringResource(editState.feel.nameId)
                ) {
                    FeelSelector(
                        feel = editState.feel,
                        onChange = { editState = editState.copy(feel = it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }

            AnimatedVisibility(visible = editState.activitySelectorState.activityType?.hasDistance == true) {
                TextField(
                    value = editState.distanceString,
                    onValueChange = { editState = editState.copy(distanceString = it) },
                    isError = editState.isDistanceStringError,
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

            AnimatedVisibility(visible = editState.activitySelectorState.activityType?.hasIntensity == true) {
                OptionGroup(
                    label = stringResource(R.string.select_intensity),
                    selectionLabel = editState.intensity?.value?.toString()
                ) {
                    Slider(
                        value = editState.intensity?.value?.toFloat() ?: -1f,
                        onValueChange = {
                            editState = editState.copy(
                                intensity = if (it < 0) {
                                    null
                                } else {
                                    Intensity(it.roundToInt().toByte())
                                }
                            )
                        },
                        steps = 10,
                        valueRange = -1f..10f
                    )
                }
            }

            TextField(
                value = editState.description,
                onValueChange = { editState = editState.copy(description = it) },
                placeholder = { Text(stringResource(R.string.placeholder_description)) },
                keyboardOptions = remember { KeyboardOptions(capitalization = KeyboardCapitalization.Sentences) },
                colors = TextFieldDefaults.colors(unfocusedContainerColor = optionGroupDefaultBackground()),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(128.dp))
        }

        val imageUri = editState.imageName?.getImageUri()
        if (showImageViewer && imageUri != null) {
            ImageViewer(
                imageUri = imageUri,
                onDismiss = { showImageViewer = false },
                onDelete = {
                    editState = editState.copy(imageName = null)
                    showImageViewer = false
                },
            )
        }
    }
}

@Composable
private fun DateTimeInput(
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
        modifier = modifier
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

@Stable
@Immutable
@Parcelize
private data class ActivityEditState(
    val activitySelectorState: ActivitySelectorState,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val description: String,
    val distanceString: String,
    val intensity: Intensity?,
    val feel: Feel,
    val imageName: ImageName?,
    val favorite: Boolean,
) : Parcelable {
    constructor(
        activity: RichActivity?,
        prediction: DecisionTrees.Prediction,
        now: LocalDateTime = LocalDateTime.now()
    ) : this(
        activitySelectorState = ActivitySelectorState(
            activityType = activity?.type ?: prediction.activityType,
            vehicle = activity?.activity?.vehicle ?: prediction.vehicle,
            place = activity?.place ?: prediction.place
        ),
        startDateTime = activity?.activity?.startTime?.toLocalDateTime() ?: now,
        endDateTime = activity?.activity?.endTime?.toLocalDateTime() ?: now.plusHours(1),
        description = activity?.activity?.description ?: "",
        distanceString = activity?.activity?.distance?.kilometers?.toString() ?: "",
        intensity = activity?.activity?.intensity,
        feel = activity?.activity?.feel ?: Feel.Ok,
        imageName = activity?.activity?.imageName,
        favorite = activity?.activity?.favorite ?: false
    )

    @IgnoredOnParcel
    val isDistanceStringError =
        distanceString.isNotBlank() && kilometerStringToDistance(distanceString) == null

    @IgnoredOnParcel
    val isEndDateTimeError = endDateTime.isBefore(startDateTime)

    @IgnoredOnParcel
    val isValid = activitySelectorState.isValid()
            && !isDistanceStringError
            && !isEndDateTimeError

    /**
     * Converts this state into an activity. Panics if the state does not represent a valid activity
     */
    fun toActivity(initialActivity: RichActivity?): RichActivity {
        val oldActivity = when (initialActivity) {
            null -> Activity(typeId = 0, startTime = startDateTime.toDate())
            else -> initialActivity.activity
        }

        val newActivity = oldActivity.copy(
            typeId = activitySelectorState.activityType?.uid!!,
            placeId = activitySelectorState.place?.uid,
            vehicle = activitySelectorState.vehicle,
            description = description,
            startTime = startDateTime.toDate(),
            endTime = endDateTime.toDate(),
            imageName = imageName,
            feel = feel,
            distance = kilometerStringToDistance(distanceString),
            intensity = intensity,
            favorite = favorite
        )

        return RichActivity(
            activity = newActivity,
            place = activitySelectorState.place,
            type = activitySelectorState.activityType,
        )
    }
}