package com.inky.fitnesscalendar.ui.views

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.Activity
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.Recording
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.components.CompactActivityCard
import com.inky.fitnesscalendar.ui.components.NewActivityFAB
import com.inky.fitnesscalendar.ui.components.Timer
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.util.Duration
import com.inky.fitnesscalendar.util.Duration.Companion.until
import com.inky.fitnesscalendar.util.showRecordingNotification
import com.inky.fitnesscalendar.view_model.HomeViewModel

const val TAG = "HOME"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    viewModel: HomeViewModel = hiltViewModel(),
    isNewActivityOpen: Boolean,
    onNewActivity: () -> Unit,
    onRecordActivity: () -> Unit,
    onNavigateActivity: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val weeklyStats by viewModel.weekStats.collectAsState(initial = null)
    val monthlyStats by viewModel.monthStats.collectAsState(initial = null)
    val activitiesToday by viewModel.activitiesToday.collectAsState(initial = null)
    val recordings by viewModel.recordings.collectAsState(initial = null)
    val scrollState = rememberScrollState()

    val context = LocalContext.current
    LaunchedEffect(recordings) {
        for (recording in recordings ?: emptyList()) {
            if (recording.uid == null) {
                continue
            }
            Log.d(TAG, "Notification for $recording")
            context.showRecordingNotification(
                recording.uid,
                recording.type,
                recording.startTime.time
            )
        }
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = {
            Text(
                stringResource(R.string.app_name),
            )
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ), navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = stringResource(R.string.Menu),
                )
            }
        }, actions = {
            IconButton(
                onClick = { onRecordActivity() },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = contentColorFor(MaterialTheme.colorScheme.primary)
                )
            ) {
                Icon(Icons.Filled.PlayArrow, stringResource(R.string.record))
            }
        }, modifier = Modifier.sharedBounds(SharedContentKey.AppBar)
        )
    }, floatingActionButton = {
        NewActivityFAB(
            onClick = onNewActivity, menuOpen = isNewActivityOpen
        )
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {

            if (recordings != null) {
                AnimatedVisibility(visible = recordings?.isNotEmpty() ?: false) {
                    Recordings(
                        recordings = recordings ?: emptyList(),
                        localizationRepository = viewModel.repository.localizationRepository,
                        onAbort = { viewModel.abortRecording(it) },
                        onSave = { viewModel.saveRecording(it) }

                    )
                }
            }

            StatisticsIfNotNull(stringResource(R.string.this_week), weeklyStats)
            StatisticsIfNotNull(stringResource(R.string.this_month), monthlyStats)

            ActivitiesTodayOrNull(
                activities = activitiesToday,
                localizationRepository = viewModel.repository.localizationRepository,
                onNavigateActivity = onNavigateActivity
            )
        }
    }
}

@Composable
fun Recordings(
    recordings: List<Recording>,
    localizationRepository: LocalizationRepository,
    onAbort: (Recording) -> Unit,
    onSave: (Recording) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier
            .padding(all = 8.dp)
            .fillMaxWidth()
    ) {
        for (recording in recordings) {
            Timer { time ->
                RecordingStatus(
                    recording,
                    localizationRepository,
                    time,
                    onAbort = { onAbort(recording) },
                    onSave = { onSave(recording) }
                )
            }
        }
    }
}

@Composable
fun RecordingStatus(
    recording: Recording,
    localizationRepository: LocalizationRepository,
    currentTimeMs: Long,
    onAbort: () -> Unit,
    onSave: () -> Unit
) {
    val timeString by remember {
        derivedStateOf {
            localizationRepository.formatRelativeDate(recording.startTime)
        }
    }
    val durationString by remember(currentTimeMs) {
        derivedStateOf {
            localizationRepository.formatDuration(recording.startTime)
        }
    }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(all = 8.dp)
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier.weight(0.75f)) {
            Icon(
                painterResource(R.drawable.record_24),
                stringResource(R.string.recording),
                tint = Color.Red,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Text(
                stringResource(recording.type.nameId),
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
        Text(timeString, modifier = Modifier.weight(0.5f))
        Text(durationString, modifier = Modifier.weight(0.5f))
        Row {
            TextButton(onClick = onAbort) {
                Text(stringResource(R.string.abort))
            }
            TextButton(onClick = onSave) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Composable
fun StatisticsIfNotNull(name: String, stats: ActivityStatistics?) {
    if (stats != null) {
        Statistics(name = name, stats = stats)
    }
}

@Composable
fun Statistics(name: String, stats: ActivityStatistics) {
    Card(
        onClick = { /*TODO*/ },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier
            .padding(all = 8.dp)
            .fillMaxWidth()
    ) {
        Text(
            name,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        if (stats.activities.isNotEmpty()) {
            for ((activityClass, activities) in stats.activitiesByClass) {
                if (activities.isEmpty()) {
                    continue
                }

                val durationString = remember(activities) {
                    Duration(activities.map { it.startTime until it.endTime }
                        .sumOf { it.elapsedMs }).format()

                }

                Row(
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        activityClass.emoji + stringResource(activityClass.nameId),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        pluralStringResource(
                            R.plurals.num_activities, activities.size, activities.size
                        ), modifier = Modifier.weight(1f)
                    )
                    Row(modifier = Modifier.weight(1f)) {
                        Icon(
                            painterResource(R.drawable.outline_timer_24),
                            stringResource(R.string.time)
                        )
                        Text(durationString)
                    }
                }
            }
        } else {
            Row(modifier = Modifier.padding(all = 8.dp)) {
                Icon(Icons.Outlined.Info, stringResource(R.string.info))
                Text("No activities in this period", modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}

@Composable
fun ActivitiesTodayOrNull(
    activities: List<Activity>?,
    localizationRepository: LocalizationRepository,
    onNavigateActivity: () -> Unit
) {
    if (activities != null) {
        ActivitiesToday(
            activities = activities,
            localizationRepository = localizationRepository,
            onNavigateActivity = onNavigateActivity
        )
    }
}

@Composable
fun ActivitiesToday(
    activities: List<Activity>,
    localizationRepository: LocalizationRepository,
    onNavigateActivity: () -> Unit
) {
    val isEmpty = activities.isEmpty()

    AnimatedVisibility(visible = !isEmpty) {
        Card(
            onClick = onNavigateActivity, colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ), modifier = Modifier
                .padding(all = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.today),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                    )
            )

            for (activity in activities) {
                CompactActivityCard(
                    activity = activity,
                    localizationRepository = localizationRepository,
                )
            }
        }
    }
}