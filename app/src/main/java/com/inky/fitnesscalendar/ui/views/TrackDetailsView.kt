package com.inky.fitnesscalendar.ui.views

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.gpx.GpxTrackStats
import com.inky.fitnesscalendar.data.gpx.TrackSvg
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.components.TrackView
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.view_model.BaseViewModel

@Composable
fun TrackDetailsView(
    viewModel: BaseViewModel = hiltViewModel(),
    activityId: Int,
    onEdit: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity by remember(activityId) { viewModel.repository.getActivity(activityId) }.collectAsState(
        null
    )
    val track by remember(activityId) { viewModel.repository.getTrackByActivity(activityId) }.collectAsState(
        null
    )
    val preview = remember(activity) { activity?.activity?.trackPreview?.toTrackSvg() }
    val stats = remember(track) { track?.computeStatistics() }
    val state = remember(stats, activity) {
        if (activity != null && stats != null) {
            DetailsState.initialize(
                activity!!,
                stats,
                viewModel.repository.localizationRepository,
                context
            )
        } else {
            null
        }
    }

    if (state != null && preview != null) {
        TrackDetailsView(
            preview = preview,
            state = state,
            onEdit = onEdit,
            onBack = onBack,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackDetailsView(
    preview: TrackSvg,
    state: DetailsState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(state.type.name) },
                colors = defaultTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, stringResource(R.string.Edit))
                    }
                },
                scrollBehavior = scrollBehavior,
                modifier = Modifier.sharedBounds(SharedContentKey.AppBar)
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .padding(all = 8.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                TrackView(
                    track = preview,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                        .aspectRatio(4f / 3)
                )
            }

            StatsColumn(title = { Text(stringResource(R.string.Time)) }) {
                SimpleStatistic(R.string.Date, state.start)
                SimpleStatistic(R.string.Duration, state.duration)
                SimpleStatistic(R.string.Moving_duration, state.movingDuration)
            }

            if (state.distance != null) {
                StatsColumn(title = { Text(stringResource(R.string.Distance)) }) {
                    Statistic(label = {}) { Text(state.distance) }
                }
            }

            if (state.hasSpeed) {
                StatsColumn(title = { Text(stringResource(R.string.Velocity)) }) {
                    SimpleStatistic(R.string.Average_velocity, state.averageSpeed)
                    SimpleStatistic(R.string.Average_moving_velocity, state.averageMovingSpeed)
                }
            }

            if (state.hasElevation) {
                StatsColumn(title = { Text(stringResource(R.string.Elevation)) }) {
                    SimpleStatistic(R.string.Minimum_elevation, state.minElevation)
                    SimpleStatistic(R.string.Maximum_elevation, state.maxElevation)
                }
            }

            if (state.hasHeartRate) {
                StatsColumn(title = { Text(stringResource(R.string.Heart_rate)) }) {
                    SimpleStatistic(R.string.Average_heart_rate, state.averageHeartRate)
                    SimpleStatistic(R.string.Maximum_heart_rate, state.maxHeartRate)
                }
            }

            if (state.hasTemperature) {
                StatsColumn(title = { Text(stringResource(R.string.Temperature)) }) {
                    SimpleStatistic(R.string.Average_temperature, state.averageTemperature)
                    SimpleStatistic(R.string.Minimum_temperature, state.minTemperature)
                    SimpleStatistic(R.string.Maximum_temperature, state.maxTemperature)
                }
            }
        }
    }
}

@Composable
private fun StatsColumn(
    title: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .padding(all = 8.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .fillMaxWidth(),
    ) {
        Box(modifier = Modifier.padding(all = 4.dp)) {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleLarge) {
                title()
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

data class DetailsState(
    val type: ActivityType,
    val start: String,
    val duration: String,
    val movingDuration: String?,
    val distance: String?,
    val averageSpeed: String?,
    val averageMovingSpeed: String?,
    val minElevation: String?,
    val maxElevation: String?,
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
            localizationRepository: LocalizationRepository,
            context: Context
        ): DetailsState {
            val activity = richActivity.activity
            val type = richActivity.type

            val startDate = localizationRepository.formatRelativeDate(activity.startTime)
            val startTime = localizationRepository.timeFormatter.format(activity.startTime)
            val start = "$startDate $startTime"

            val duration = activity.duration.format()
            val movingDuration = activity.movingDuration?.format()

            val distance = activity.distance?.format(context)

            val averageSpeed = activity.averageSpeed?.format(context)
            val averageMovingSpeed = activity.averageMovingSpeed?.format(context)

            val minHeight = stats.minHeight?.format(context)
            val maxHeight = stats.maxHeight?.format(context)

            val averageHeartRate = activity.averageHeartRate?.format(context)
            val maxHeartRate = activity.maximalHeartRate?.format(context)

            val minTemperature = stats.minTemperature?.format(context)
            val maxTemperature = stats.maxTemperature?.format(context)
            val averageTemperature = activity.temperature?.format(context)

            return DetailsState(
                type = type,
                start = start,
                duration = duration,
                movingDuration = movingDuration,
                distance = distance,
                averageSpeed = averageSpeed,
                averageMovingSpeed = averageMovingSpeed,
                averageHeartRate = averageHeartRate,
                maxHeartRate = maxHeartRate,
                minTemperature = minTemperature,
                maxTemperature = maxTemperature,
                averageTemperature = averageTemperature,
                minElevation = minHeight,
                maxElevation = maxHeight
            )
        }
    }
}