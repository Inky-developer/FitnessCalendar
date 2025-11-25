package com.inky.fitnesscalendar.ui.views

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ImageName
import com.inky.fitnesscalendar.data.gpx.GpxTrackStats
import com.inky.fitnesscalendar.data.gpx.TrackSvg
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.repository.DatabaseRepository
import com.inky.fitnesscalendar.ui.components.ActivityImages
import com.inky.fitnesscalendar.ui.components.FavoriteIcon
import com.inky.fitnesscalendar.ui.components.ImageLimit
import com.inky.fitnesscalendar.ui.components.ImageViewer
import com.inky.fitnesscalendar.ui.components.OkayCancelDialog
import com.inky.fitnesscalendar.ui.components.SelectImageDropdownMenuItem
import com.inky.fitnesscalendar.ui.components.TrackView
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.util.Icons
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.ui.util.sharedElement
import com.inky.fitnesscalendar.util.NonEmptyList
import com.inky.fitnesscalendar.util.asNonEmptyOrNull
import com.inky.fitnesscalendar.util.toLocalDate
import com.inky.fitnesscalendar.view_model.BaseViewModel
import kotlinx.coroutines.launch

@Composable
fun TrackDetailsView(
    viewModel: BaseViewModel = hiltViewModel(),
    activityId: Int,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onNavigateMap: (Int) -> Unit,
    onNavigateGraph: (Int, TrackGraphProjection) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val state = rememberDetailsState(activityId, viewModel.repository)

    if (state != null) {
        TrackDetailsView(
            state = state,
            onUpdate = {
                scope.launch {
                    val newActivity = state.getUpdatedActivity()
                    viewModel.repository.saveActivity(newActivity)
                }
            },
            onShare = onShare,
            onBack = onBack,
            onNavigateMap = { onNavigateMap(activityId) },
            onNavigateGraph = { projection -> onNavigateGraph(activityId, projection) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackDetailsView(
    state: DetailsState,
    onUpdate: () -> Unit,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onNavigateMap: () -> Unit,
    onNavigateGraph: (TrackGraphProjection) -> Unit
) {
    var showUnsavedChangesDialog by rememberSaveable { mutableStateOf(false) }
    ConfirmUnsavedChangesOnBack(
        enabled = state.hasChanged.value,
        showDialog = showUnsavedChangesDialog,
        onShowDialog = { showUnsavedChangesDialog = it },
        onBack = onBack
    )

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.data.type.name) },
                colors = defaultTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.hasChanged.value) {
                            showUnsavedChangesDialog = true
                        } else {
                            onBack()
                        }
                    }) {
                        Icons.ArrowBack(stringResource(R.string.back))
                    }
                },
                actions = {
                    var contextMenuOpen by rememberSaveable { mutableStateOf(false) }

                    IconButton(onClick = state::toggleIsFavorite) {
                        FavoriteIcon(state.editState.isFavorite)
                    }
                    IconButton(onClick = onShare) {
                        Icons.Share(stringResource(R.string.share))
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
                                state.addImages(images)
                            },
                            onDismissMenu = { contextMenuOpen = false },
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                modifier = Modifier.sharedBounds(SharedContentKey.AppBar)
            )
        },
        floatingActionButton = {
            var isSaving by rememberSaveable(state.hasChanged.value) { mutableStateOf(false) }

            AnimatedVisibility(
                visible = state.hasChanged.value && !isSaving,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        isSaving = true
                        onUpdate()
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
                .fillMaxWidth()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            TrackDetailsData(
                state = state,
                onNavigateMap = onNavigateMap,
                onNavigateGraph = onNavigateGraph
            )
        }
    }
}

@Composable
private fun ConfirmUnsavedChangesOnBack(
    enabled: Boolean,
    showDialog: Boolean,
    onShowDialog: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(enabled = enabled) {
        onShowDialog(true)
    }

    if (showDialog) {
        OkayCancelDialog(
            onDismiss = { onShowDialog(false) },
            onOkay = {
                onShowDialog(false)
                onBack()
            }
        ) {
            Text(stringResource(R.string.Unsaved_changes_warning))
        }
    }
}

@Composable
fun TrackDetailsData(
    state: DetailsState, onNavigateMap: () -> Unit, onNavigateGraph: (TrackGraphProjection) -> Unit
) {
    var imagePopup by rememberSaveable { mutableStateOf<ImageName?>(null) }

    val images = state.editState.images.asNonEmptyOrNull()
    if (images != null) {
        ActivityImages(
            images,
            onClick = { imagePopup = it },
            modifier = Modifier
                .padding(all = 8.dp)
                .sharedElement(SharedContentKey.ActivityImage(state.initialActivity.activity.uid!!))
        )
    }

    imagePopup?.let { image ->
        ImageViewer(
            imageUri = image.getImageUri(),
            onDismiss = { imagePopup = null },
            onDelete = {
                state.removeImage(image)
                imagePopup = null
            }
        )
    }

    Box(
        modifier = Modifier
            .padding(all = 8.dp)
            .clip(MaterialTheme.shapes.medium)
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable { onNavigateMap() }
    ) {
        TrackView(
            track = state.data.preview,
            color = Color.Black,
            modifier = Modifier
                .padding(all = 8.dp)
                .fillMaxWidth()
                .aspectRatio(4f / 3)
                .sharedBounds(SharedContentKey.Map)
        )
    }

    ActivityDescription(
        description = state.editState.description,
        onDescription = { state.editState = state.editState.copy(description = it) }
    )

    StatsColumn(title = { Text(stringResource(R.string.Time)) }) {
        SimpleStatistic(R.string.Date, state.data.start)
        SimpleStatistic(R.string.Duration, state.data.duration)
        SimpleStatistic(R.string.Moving_duration, state.data.movingDuration)
    }

    if (state.data.distance != null) {
        StatsColumn(title = { Text(stringResource(R.string.Distance)) }) {
            Statistic(label = {}) { Text(state.data.distance) }
        }
    }

    if (state.data.hasSpeed) {
        StatsColumn(
            title = { Text(stringResource(R.string.Velocity)) },
            onClick = { onNavigateGraph(TrackGraphProjection.Speed) }
        ) {
            SimpleStatistic(R.string.Average_velocity, state.data.averageSpeed)
            SimpleStatistic(R.string.Average_moving_velocity, state.data.averageMovingSpeed)
            SimpleStatistic(R.string.Maximum_velocity, state.data.maxSpeed)
        }
    }

    if (state.data.hasElevation) {
        StatsColumn(
            title = { Text(stringResource(R.string.Elevation)) },
            onClick = { onNavigateGraph(TrackGraphProjection.Elevation) }
        ) {
            SimpleStatistic(R.string.Minimum_elevation, state.data.minElevation)
            SimpleStatistic(R.string.Maximum_elevation, state.data.maxElevation)
            SimpleStatistic(R.string.Total_ascent, state.data.totalAscent)
            SimpleStatistic(R.string.Total_descent, state.data.totalDescent)
        }
    }

    if (state.data.hasHeartRate) {
        StatsColumn(
            title = { Text(stringResource(R.string.Heart_rate)) },
            onClick = { onNavigateGraph(TrackGraphProjection.HeartRate) }
        ) {
            SimpleStatistic(R.string.Average_heart_rate, state.data.averageHeartRate)
            SimpleStatistic(R.string.Maximum_heart_rate, state.data.maxHeartRate)
        }
    }

    if (state.data.hasTemperature) {
        StatsColumn(
            title = { Text(stringResource(R.string.Temperature)) },
            onClick = { onNavigateGraph(TrackGraphProjection.Temperature) }
        ) {
            SimpleStatistic(R.string.Average_temperature, state.data.averageTemperature)
            SimpleStatistic(R.string.Minimum_temperature, state.data.minTemperature)
            SimpleStatistic(R.string.Maximum_temperature, state.data.maxTemperature)
        }
    }
}

@Composable
private fun Modifier.statsEntry() = this
    .padding(all = 8.dp)
    .clip(MaterialTheme.shapes.medium)
    .background(MaterialTheme.colorScheme.surfaceContainer)
    .fillMaxWidth()
    .padding(all = 4.dp)

@Composable
private fun ActivityDescription(description: String, onDescription: (String) -> Unit) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val modifier = Modifier
        .statsEntry()
        .clickable { showDialog = true }

    AnimatedContent(description.isBlank()) { isBlank ->
        if (isBlank) {
            Text(
                text = stringResource(R.string.tap_to_add_description),
                color = MaterialTheme.colorScheme.secondary,
                modifier = modifier
            )
        } else {
            Text(text = description, modifier = modifier)
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            TextField(value = description, onValueChange = onDescription)
        }
    }
}

@Composable
private fun StatsColumn(
    title: @Composable () -> Unit,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .statsEntry()
            .clickable { onClick?.let { it() } }
    ) {
        Row(modifier = Modifier.padding(all = 4.dp)) {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleLarge) {
                title()
            }
            Spacer(modifier = Modifier.weight(1f))
            if (onClick != null) {
                Icon(
                    painterResource(R.drawable.outline_show_chart_24),
                    stringResource(R.string.track_graph)
                )
            }
        }
        HorizontalDivider()
        Column(modifier = Modifier.padding(all = 4.dp)) {
            content()
        }
    }
}

@Composable
private fun Statistic(label: @Composable () -> Unit, content: @Composable () -> Unit) {
    Column {
        content()
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.labelSmall) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.secondary) {
                label()
            }
        }
    }
}

@Composable
private fun SimpleStatistic(@StringRes labelRes: Int, value: String?) {
    if (value != null) {
        Statistic(label = { Text(stringResource(labelRes)) }) {
            Text(value)
        }
    }
}

@Composable
private fun rememberDetailsState(
    activityId: Int,
    repository: DatabaseRepository,
): DetailsState? {
    val context = LocalContext.current

    val activity by remember(activityId) { repository.getActivity(activityId) }.collectAsState(null)
    val track by remember(activityId) { repository.getTrackByActivity(activityId) }.collectAsState(
        null
    )
    val preview = remember(activity) { activity?.activity?.trackPreview?.toTrackSvg() }
    val stats = remember(track) { track?.computeStatistics()?.ok() }

    return whenNonNull(activity, track, preview, stats) { activity, track, preview, stats ->
        remember(activity, track) {
            DetailsState(
                activity,
                stats,
                preview,
                repository.localizationRepository,
                context
            )
        }
    }
}

private inline fun <A, B, C, D, T> whenNonNull(
    a: A?,
    b: B?,
    c: C?,
    d: D?,
    block: (A, B, C, D) -> T
): T? =
    if (a != null && b != null && c != null && d != null) {
        block(a, b, c, d)
    } else {
        null
    }


class DetailsState(
    editStateImpl: MutableState<DetailsEditState>,
    val initialEditState: DetailsEditState,
    val initialActivity: RichActivity,
    val data: DetailsData
) {
    constructor(
        richActivity: RichActivity,
        stats: GpxTrackStats,
        preview: TrackSvg,
        localizationRepository: LocalizationRepository,
        context: Context
    ) : this(
        initialEditState = DetailsEditState(richActivity),
        initialActivity = richActivity,
        editStateImpl = mutableStateOf(DetailsEditState(richActivity)),
        data = DetailsData.initialize(richActivity, stats, preview, localizationRepository, context)
    )

    var editState by editStateImpl

    val hasChanged = derivedStateOf { editState != initialEditState }

    fun removeImage(image: ImageName) {
        editState = editState.copy(images = editState.images - image)
    }

    fun addImages(images: NonEmptyList<ImageName>) {
        editState = editState.copy(images = editState.images + images)
    }

    fun toggleIsFavorite() {
        editState = editState.copy(isFavorite = !editState.isFavorite)
    }

    fun getUpdatedActivity() = editState.getActivity(initialActivity)
}

data class DetailsEditState(
    val images: List<ImageName>,
    val description: String,
    val isFavorite: Boolean
) {
    constructor(richActivity: RichActivity) : this(
        images = richActivity.images,
        description = richActivity.activity.description,
        isFavorite = richActivity.activity.favorite
    )

    fun getActivity(initialActivity: RichActivity) = initialActivity.copy(
        images = images,
        activity = initialActivity.activity.copy(
            description = description,
            favorite = isFavorite
        )
    )
}

data class DetailsData(
    val preview: TrackSvg,
    val type: ActivityType,
    val start: String,
    val duration: String,
    val movingDuration: String?,
    val distance: String?,
    val averageSpeed: String?,
    val averageMovingSpeed: String?,
    val maxSpeed: String?,
    val minElevation: String?,
    val maxElevation: String?,
    val totalAscent: String?,
    val totalDescent: String?,
    val averageHeartRate: String?,
    val maxHeartRate: String?,
    val minTemperature: String?,
    val maxTemperature: String?,
    val averageTemperature: String?,
) {
    val hasSpeed get() = averageSpeed != null || averageMovingSpeed != null
    val hasElevation get() = minElevation != null && maxElevation != null
    val hasHeartRate get() = averageHeartRate != null
    val hasTemperature get() = averageTemperature != null

    companion object {
        fun initialize(
            richActivity: RichActivity,
            stats: GpxTrackStats,
            preview: TrackSvg,
            localizationRepository: LocalizationRepository,
            context: Context
        ): DetailsData {
            val activity = richActivity.activity
            val type = richActivity.type

            val startDate =
                localizationRepository.formatRelativeLocalDate(activity.startTime.toLocalDate())
            val startTime = localizationRepository.timeFormatter.format(activity.startTime)
            val start = "$startDate $startTime"

            val duration = activity.duration.format()
            val movingDuration = activity.movingDuration?.format()

            val distance = activity.distance?.formatWithContext(context)

            val averageSpeed = activity.averageSpeed?.formatWithContext(context)
            val averageMovingSpeed = activity.averageMovingSpeed?.formatWithContext(context)
            val maxSpeed = stats.maxSpeed.formatWithContext(context)

            val minHeight = stats.minHeight?.formatWithContext(context)
            val maxHeight = stats.maxHeight?.formatWithContext(context)
            val totalAscent = stats.totalAscent?.formatWithContext(context)
            val totalDescent = stats.totalDescent?.formatWithContext(context)

            val averageHeartRate = activity.averageHeartRate?.formatWithContext(context)
            val maxHeartRate = activity.maximalHeartRate?.formatWithContext(context)

            val minTemperature = stats.minTemperature?.formatWithContext(context)
            val maxTemperature = stats.maxTemperature?.formatWithContext(context)
            val averageTemperature = activity.temperature?.formatWithContext(context)

            return DetailsData(
                preview = preview,
                type = type,
                start = start,
                duration = duration,
                movingDuration = movingDuration,
                distance = distance,
                averageSpeed = averageSpeed,
                averageMovingSpeed = averageMovingSpeed,
                maxSpeed = maxSpeed,
                averageHeartRate = averageHeartRate,
                maxHeartRate = maxHeartRate,
                minTemperature = minTemperature,
                maxTemperature = maxTemperature,
                averageTemperature = averageTemperature,
                minElevation = minHeight,
                maxElevation = maxHeight,
                totalAscent = totalAscent,
                totalDescent = totalDescent
            )
        }
    }
}