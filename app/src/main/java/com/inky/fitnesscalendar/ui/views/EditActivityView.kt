package com.inky.fitnesscalendar.ui.views

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImagePainter
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.data.ImageName
import com.inky.fitnesscalendar.data.Intensity
import com.inky.fitnesscalendar.data.measure.format
import com.inky.fitnesscalendar.data.measure.kilometers
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.preferences.Preference
import com.inky.fitnesscalendar.ui.components.ActivityImages
import com.inky.fitnesscalendar.ui.components.ActivitySelector
import com.inky.fitnesscalendar.ui.components.ActivitySelectorState
import com.inky.fitnesscalendar.ui.components.DateTimePicker
import com.inky.fitnesscalendar.ui.components.DescriptionTextInput
import com.inky.fitnesscalendar.ui.components.DurationPicker
import com.inky.fitnesscalendar.ui.components.FavoriteIcon
import com.inky.fitnesscalendar.ui.components.FeelSelector
import com.inky.fitnesscalendar.ui.components.ImageLimit
import com.inky.fitnesscalendar.ui.components.ImageViewer
import com.inky.fitnesscalendar.ui.components.OkayCancelDialog
import com.inky.fitnesscalendar.ui.components.OptionGroup
import com.inky.fitnesscalendar.ui.components.SelectImageDropdownMenuItem
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.components.optionGroupDefaultBackground
import com.inky.fitnesscalendar.ui.util.Icons
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.applyIf
import com.inky.fitnesscalendar.ui.util.localPreferences
import com.inky.fitnesscalendar.ui.util.sharedElement
import com.inky.fitnesscalendar.util.Option
import com.inky.fitnesscalendar.util.asNonEmptyOrNull
import com.inky.fitnesscalendar.util.toDate
import com.inky.fitnesscalendar.util.toLocalDateTime
import com.inky.fitnesscalendar.view_model.NewActivityViewModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.roundToInt

@Composable
fun NewActivity(
    activityId: Int?,
    viewModel: NewActivityViewModel = hiltViewModel(),
    onSave: (RichActivity) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateNewPlace: () -> Unit,
    initialDay: EpochDay?
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
            initialDay = initialDay
        )
    } else {
        CircularProgressIndicator()
    }
}

@Composable
fun NewActivity(
    richActivity: RichActivity?,
    localizationRepository: LocalizationRepository,
    onSave: (RichActivity) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateNewPlace: () -> Unit,
    initialDay: EpochDay? = null,
    isTest: Boolean = false
) {
    val initialState = remember {
        var state = ActivityEditState(richActivity)
        if (initialDay != null) {
            val newStartDateTime =
                initialDay.toLocalDate().atTime(state.startDateTime.toLocalTime())
            state = state.copy(
                startDateTime = newStartDateTime,
                endDateTime = newStartDateTime + Duration.between(
                    state.startDateTime,
                    state.endDateTime
                ),
            )
        }
        state
    }
    var editState by rememberSaveable { mutableStateOf(initialState) }
    NewActivity(
        editState = editState,
        onState = { editState = it },
        initialState = initialState,
        localizationRepository = localizationRepository,
        onSave = { onSave(editState.toRichActivity(initialActivity = richActivity)) },
        onNavigateBack = onNavigateBack,
        onNavigateNewPlace = onNavigateNewPlace,
        isTest = isTest
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewActivity(
    editState: ActivityEditState,
    onState: (ActivityEditState) -> Unit,
    initialState: ActivityEditState,
    localizationRepository: LocalizationRepository,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateNewPlace: () -> Unit,
    isTest: Boolean = false
) {
    var showBackDialog by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    val title by remember(editState) {
        derivedStateOf {
            if (editState.activityId != null && editState.activitySelectorState.activityType != null) {
                context.getString(
                    R.string.edit_object,
                    editState.activitySelectorState.activityType.name
                )
            } else {
                context.getString(R.string.new_activity)
            }
        }
    }

    val scrollState = rememberScrollState()
    var contextMenuOpen by rememberSaveable { mutableStateOf(false) }
    var imageViewerImage by rememberSaveable { mutableStateOf<ImageName?>(null) }

    val isKeyboardVisible = WindowInsets.isImeVisible
    // TODO: Get rid of the `isTest` hack
    // In testing, the ime is always visible for some reason which means that the save button can never be clicked.
    val showSaveButton by remember(isKeyboardVisible, editState) {
        derivedStateOf {
            (!isKeyboardVisible || isTest) && (editState.activityId == null || editState != initialState) && editState.isValid
        }
    }

    BackHandler(enabled = editState != initialState) {
        showBackDialog = true
    }

    val onBack = remember {
        {
            if (editState != initialState) {
                showBackDialog = true
            } else {
                onNavigateBack()
            }
        }
    }

    if (showBackDialog) {
        OkayCancelDialog(
            onDismiss = { showBackDialog = false },
            onOkay = {
                showBackDialog = false
                onNavigateBack()
            }
        ) {
            Text(stringResource(R.string.Unsaved_changes_warning))
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                colors = defaultTopAppBarColors(),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icons.ArrowBack(stringResource(R.string.back))
                    }
                },
                actions = {
                    IconToggleButton(
                        checked = editState.favorite,
                        onCheckedChange = { onState(editState.copy(favorite = it)) }
                    ) {
                        AnimatedContent(editState.favorite, label = "Favorite") { isFavorite ->
                            FavoriteIcon(isFavorite)
                        }
                    }
                    IconButton(onClick = { contextMenuOpen = true }) {
                        Icons.MoreOptions(stringResource(R.string.open_context_menu))
                    }
                    DropdownMenu(
                        expanded = contextMenuOpen,
                        onDismissRequest = { contextMenuOpen = false }
                    ) {
                        SelectImageDropdownMenuItem(
                            imageLimit = ImageLimit.Multiple,
                            onImages = { images ->
                                onState(editState.copy(images = editState.images + images.map {
                                    ActivityEditState.ImageState(it)
                                }))
                            },
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
                            onSave()
                        }
                    },
                    modifier = Modifier.testTag("button-confirm")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icons.Check(stringResource(R.string.save))
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
            val imageNames = editState.images.map { it.imageName }.asNonEmptyOrNull()
            if (imageNames != null) {
                ActivityImages(
                    images = imageNames,
                    onState = { image, state ->
                        if (state is AsyncImagePainter.State.Error && !initialState.images.any { it.imageName == image }) {
                            onState(editState.copy(images = editState.images.filter { it.imageName != image }))
                        }
                    },
                    onClick = { imageViewerImage = it },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .applyIf(editState.activityId != null) {
                            sharedElement(SharedContentKey.ActivityImage(editState.activityId!!))
                        }
                )
            }

            AnimatedContent(
                targetState = editState.activitySelectorState.activityType?.hasDuration == true,
                label = stringResource(R.string.select_date)
            ) { hasDuration ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    val dateLabelId = if (hasDuration) {
                        R.string.datetime_start
                    } else {
                        R.string.datetime_date
                    }
                    DateTimeInput(
                        dateTime = editState.startDateTime,
                        localizationRepository = localizationRepository,
                        labelId = dateLabelId,
                        onDateTime = { onState(editState.copy(startDateTime = it)) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    if (hasDuration) {
                        DateAndDurationInput(
                            editState = editState,
                            onEnd = { onState(editState.copy(endDateTime = it)) },
                            localizationRepository = localizationRepository
                        )
                    }
                }
            }

            ActivitySelector(
                editState.activitySelectorState,
                onState = { onState(editState.copy(activitySelectorState = it)) },
                onNavigateNewPlace = onNavigateNewPlace
            )

            AnimatedVisibility(visible = editState.activitySelectorState.activityType?.hasFeel() == true) {
                OptionGroup(
                    label = stringResource(R.string.select_feel),
                    selectionLabel = editState.feel.text()
                ) {
                    FeelSelector(
                        feel = editState.feel,
                        onChange = { onState(editState.copy(feel = it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }

            AnimatedVisibility(visible = editState.activitySelectorState.activityType?.hasDistance == true) {
                TextField(
                    value = editState.distanceString,
                    onValueChange = { onState(editState.copy(distanceString = it)) },
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
                            onState(
                                editState.copy(
                                    intensity = if (it < 0) {
                                        null
                                    } else {
                                        Intensity(it.roundToInt().toByte())
                                    }
                                )
                            )
                        },
                        steps = 10,
                        valueRange = -1f..10f
                    )
                }
            }

            DescriptionTextInput(
                description = editState.description,
                onDescription = { onState(editState.copy(description = it)) },
                modifier = Modifier
                    .testTag("input-description")
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(128.dp))
        }

        imageViewerImage?.let { image ->
            ImageViewer(
                imageUri = image.getImageUri(),
                onDismiss = { imageViewerImage = null },
                onDelete = {
                    onState(editState.copy(images = editState.images.filter { it.imageName != image }))
                    imageViewerImage = null
                },
            )
        }
    }
}

@Composable
private fun DateTimeInputButton(
    onClick: () -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val errorColor = MaterialTheme.colorScheme.error

    val border = remember(isError) {
        if (isError) {
            BorderStroke(width = 1.dp, color = errorColor)
        } else {
            null
        }
    }

    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            containerColor = optionGroupDefaultBackground(),
            contentColor = contentColorFor(optionGroupDefaultBackground())
        ),
        border = border,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) { content() }
}

@Composable
private fun DateTimeInput(
    dateTime: LocalDateTime,
    localizationRepository: LocalizationRepository,
    labelId: Int,
    onDateTime: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
    showDate: Boolean = true,
    isError: Boolean = false,
) {
    val dateTimeStr = remember(dateTime, showDate) {
        val date = dateTime.toDate()
        val startDateStr =
            if (showDate) localizationRepository.dateFormatter.format(date.time) else ""
        val startTimeStr = localizationRepository.timeFormatter.format(date)

        startDateStr to startTimeStr
    }

    var showPicker by rememberSaveable { mutableStateOf(false) }

    DateTimeInputButton(onClick = { showPicker = true }, isError = isError, modifier = modifier) {
        Text(
            stringResource(
                labelId,
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

@Composable
private fun DurationInput(duration: Duration, onDuration: (Duration) -> Unit, isError: Boolean) {
    var showPicker by rememberSaveable { mutableStateOf(false) }

    DateTimeInputButton(onClick = { showPicker = true }, isError = isError) {
        Text(stringResource(R.string.duration, duration.format()))
    }

    if (showPicker) {
        DurationPicker(
            duration,
            onConfirm = {
                onDuration(it)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}

@Composable
private fun RowScope.DateAndDurationInput(
    editState: ActivityEditState,
    onEnd: (LocalDateTime) -> Unit,
    localizationRepository: LocalizationRepository
) {
    val preferDuration = localPreferences.current.preferEndDateAsDuration
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    AnimatedContent(
        preferDuration,
        modifier = Modifier
            .padding(start = 8.dp)
            .weight(1f)
            .fillMaxHeight()
    ) { preferDuration ->
        if (preferDuration) {
            DurationInput(
                duration = Duration.between(editState.startDateTime, editState.endDateTime),
                onDuration = { onEnd(editState.startDateTime + it) },
                isError = editState.isEndDateTimeError
            )
        } else {
            DateTimeInput(
                dateTime = editState.endDateTime,
                showDate = editState.startDateTime.toLocalDate() != editState.endDateTime.toLocalDate(),
                localizationRepository = localizationRepository,
                labelId = R.string.datetime_end,
                onDateTime = onEnd,
                isError = editState.isEndDateTimeError,
            )
        }

    }

    IconButton(onClick = {
        scope.launch {
            Preference.PREF_PREFER_END_DATE_AS_DURATION.set(context, !preferDuration)
        }
    }) {
        AnimatedContent(preferDuration) { preferDuration ->
            if (preferDuration) {
                Icons.Timer(stringResource(R.string.switch_to_date_input))
            } else {
                Icons.CalendarToday(stringResource(R.string.switch_to_duration_input))
            }
        }
    }
}

private fun kilometerStringToDistance(string: String) = if (string.isBlank()) {
    null
} else {
    string.replace(",", ".").toDoubleOrNull()?.kilometers()
}

@Stable
@Immutable
@Parcelize
data class ActivityEditState(
    val activitySelectorState: ActivitySelectorState,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val description: String,
    val distanceString: String,
    val intensity: Intensity?,
    val feel: Feel,
    val images: List<ImageState>,
    val favorite: Boolean,

    val activityId: Int?,
) : Parcelable {
    constructor(activity: RichActivity?, now: LocalDateTime = LocalDateTime.now()) : this(
        activitySelectorState = ActivitySelectorState(
            selectedActivityType = activity?.type,
            selectedVehicle = activity?.activity?.vehicle,
            selectedPlace = activity?.let { Option.Some(it.place) } ?: Option.None
        ),
        startDateTime = activity?.activity?.startTime?.toLocalDateTime() ?: now,
        endDateTime = activity?.activity?.endTime?.toLocalDateTime() ?: now.plusHours(1),
        description = activity?.activity?.description ?: "",
        distanceString = activity?.activity?.distance?.kilometers?.toString() ?: "",
        intensity = activity?.activity?.intensity,
        feel = activity?.activity?.feel ?: Feel.Ok,
        images = activity?.images?.map { ImageState(it) } ?: emptyList(),
        favorite = activity?.activity?.favorite ?: false,
        activityId = activity?.activity?.uid
    )

    @IgnoredOnParcel
    val isDistanceStringError =
        distanceString.isNotBlank() && kilometerStringToDistance(distanceString) == null

    @IgnoredOnParcel
    val isEndDateTimeError =
        endDateTime.isBefore(startDateTime) && activitySelectorState.activityType?.hasDuration == true

    @IgnoredOnParcel
    val isValid = activitySelectorState.isValid
            && !isDistanceStringError
            && !isEndDateTimeError

    /**
     * Converts this state into an activity. Panics if the state does not represent a valid activity
     */
    fun toRichActivity(initialActivity: RichActivity?): RichActivity {
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
            feel = feel,
            distance = kilometerStringToDistance(distanceString),
            intensity = intensity,
            favorite = favorite
        )

        return RichActivity(
            activity = newActivity,
            place = activitySelectorState.place,
            type = activitySelectorState.activityType,
            images = images.map { it.imageName }
        )
    }

    @Parcelize
    data class ImageState(
        val activityId: Int?,
        val imageName: ImageName
    ) : Parcelable {
        constructor(imageName: ImageName) : this(null, imageName)
    }
}