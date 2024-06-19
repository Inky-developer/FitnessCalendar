package com.inky.fitnesscalendar.ui.views

import android.util.Log
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.Activity
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.Recording
import com.inky.fitnesscalendar.data.TypeActivity
import com.inky.fitnesscalendar.data.TypeRecording
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.components.ActivityCard
import com.inky.fitnesscalendar.ui.components.CompactActivityCard
import com.inky.fitnesscalendar.ui.components.NewActivityFAB
import com.inky.fitnesscalendar.ui.components.Timer
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.ui.util.sharedElement
import com.inky.fitnesscalendar.util.Duration
import com.inky.fitnesscalendar.util.Duration.Companion.until
import com.inky.fitnesscalendar.util.showRecordingNotification
import com.inky.fitnesscalendar.view_model.HomeViewModel
import com.inky.fitnesscalendar.view_model.statistics.Period
import kotlinx.coroutines.launch

private const val TAG = "HOME"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    viewModel: HomeViewModel = hiltViewModel(),
    isNewActivityOpen: Boolean,
    onNewActivity: () -> Unit,
    onEditActivity: (Activity) -> Unit,
    onRecordActivity: () -> Unit,
    onNavigateActivity: () -> Unit,
    onNavigateStats: (Period) -> Unit,
    onOpenDrawer: () -> Unit
) {
    val weeklyStats by viewModel.weekStats.collectAsState(initial = null)
    val monthlyStats by viewModel.monthStats.collectAsState(initial = null)
    val activitiesToday by viewModel.activitiesToday.collectAsState(initial = null)
    val recentActivity by viewModel.mostRecentActivity.collectAsState(initial = null)
    val typeRecordings by viewModel.recordings.collectAsState(initial = null)
    val scrollState = rememberScrollState()

    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    LaunchedEffect(typeRecordings) {
        for (typeRecording in typeRecordings ?: emptyList()) {
            if (typeRecording.recording.uid == null) {
                continue
            }
            Log.d(TAG, "Notification for $typeRecording")
            context.showRecordingNotification(
                typeRecording.recording.uid,
                typeRecording.type,
                typeRecording.recording.startTime.time
            )
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = stringResource(R.string.Menu),
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onRecordActivity() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = contentColorFor(MaterialTheme.colorScheme.primary)
                        )
                    ) {
                        Icon(Icons.Filled.PlayArrow, stringResource(R.string.action_record))
                    }
                },
                scrollBehavior = scrollBehavior,
                modifier = Modifier.sharedBounds(SharedContentKey.AppBar)
            )
        },
        floatingActionButton = {
            NewActivityFAB(
                onClick = onNewActivity, menuOpen = isNewActivityOpen
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {

            AnimatedVisibility(visible = typeRecordings?.isNotEmpty() ?: false) {
                Recordings(
                    typeRecordings = typeRecordings ?: emptyList(),
                    localizationRepository = viewModel.repository.localizationRepository,
                    onAbort = { viewModel.abortRecording(it) },
                    onSave = { viewModel.saveRecording(it) }

                )
            }

            RecentActivityOrNull(
                recentActivity,
                viewModel.repository.localizationRepository,
                onDelete = { scope.launch { viewModel.repository.deleteActivity(it) } },
                onEdit = onEditActivity,
            )

            StatisticsIfNotNull(
                stringResource(R.string.last_seven_days),
                weeklyStats,
                onClick = { onNavigateStats(Period.Day) })
            StatisticsIfNotNull(
                stringResource(R.string.this_month),
                monthlyStats,
                onClick = { onNavigateStats(Period.Week) }
            )

            ActivitiesToday(
                typeActivities = activitiesToday ?: emptyList(),
                localizationRepository = viewModel.repository.localizationRepository,
                onNavigateActivity = onNavigateActivity
            )
        }
    }
}

@Composable
fun Recordings(
    typeRecordings: List<TypeRecording>,
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
        for (typeRecording in typeRecordings) {
            Timer { time ->
                RecordingStatus(
                    typeRecording,
                    localizationRepository,
                    time,
                    onAbort = { onAbort(typeRecording.recording) },
                    onSave = { onSave(typeRecording.recording) }
                )
            }
        }
    }
}

@Composable
fun RecordingStatus(
    typeRecording: TypeRecording,
    localizationRepository: LocalizationRepository,
    currentTimeMs: Long,
    onAbort: () -> Unit,
    onSave: () -> Unit
) {
    val timeString by remember {
        derivedStateOf {
            localizationRepository.formatRelativeDate(typeRecording.recording.startTime)
        }
    }
    val durationString by remember(currentTimeMs) {
        derivedStateOf {
            localizationRepository.formatDuration(typeRecording.recording.startTime)
        }
    }
    Column(modifier = Modifier.padding(all = 8.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(end = 8.dp)) {
                Icon(
                    painterResource(R.drawable.record_24),
                    stringResource(R.string.recording),
                    tint = Color.Red,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Text(
                    typeRecording.type.name,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Text(timeString)
            Text(durationString, modifier = Modifier)
        }
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
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
fun StatisticsIfNotNull(name: String, stats: ActivityStatistics?, onClick: () -> Unit) {
    if (stats != null) {
        Statistics(name = name, stats = stats, onClick)
    }
}

@Composable
fun Statistics(name: String, stats: ActivityStatistics, onClick: () -> Unit) {
    Card(
        onClick = onClick,
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
            for ((activityCategory, categoryStats) in stats.activitiesByCategory) {
                if (categoryStats.isEmpty()) {
                    continue
                }

                val durationString = remember(categoryStats) {
                    Duration(categoryStats.activities.map { it.activity.startTime until it.activity.endTime }
                        .sumOf { it.elapsedMs }).format()

                }

                Row(
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        activityCategory.emoji + stringResource(activityCategory.nameId),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        pluralStringResource(
                            R.plurals.num_activities, categoryStats.size, categoryStats.size
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    Row(modifier = Modifier.weight(0.75f)) {
                        Icon(
                            painterResource(R.drawable.outline_timer_24),
                            stringResource(R.string.time)
                        )
                        Text(durationString, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
fun ActivitiesToday(
    typeActivities: List<TypeActivity>,
    localizationRepository: LocalizationRepository,
    onNavigateActivity: () -> Unit
) {

    Card(
        onClick = onNavigateActivity,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        modifier = Modifier
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

        AnimatedContent(
            targetState = typeActivities.isEmpty(),
            label = "ActivitiesToday"
        ) { noActivities ->
            when (noActivities) {
                true -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            stringResource(R.string.info),
                            modifier = Modifier.padding(end = 8.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            stringResource(R.string.no_activities_yet),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                false -> {
                    Column {
                        for (typeActivity in typeActivities) {
                            CompactActivityCard(
                                typeActivity = typeActivity,
                                localizationRepository = localizationRepository,
                                modifier = Modifier.sharedElement(
                                    SharedContentKey.ActivityCard(
                                        typeActivity.activity.uid
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentActivityOrNull(
    typeActivity: TypeActivity?,
    localizationRepository: LocalizationRepository,
    onDelete: (Activity) -> Unit,
    onEdit: (Activity) -> Unit
) {
    if (typeActivity != null) {
        RecentActivity(
            typeActivity,
            localizationRepository,
            { onDelete(typeActivity.activity) },
            { onEdit(typeActivity.activity) })
    }
}

@Composable
fun RecentActivity(
    typeActivity: TypeActivity,
    localizationRepository: LocalizationRepository,
    onDelete: () -> Unit,
    onEdit: (Activity) -> Unit,
) {
    ActivityCard(
        typeActivity = typeActivity,
        localizationRepository = localizationRepository,
        onDelete = onDelete,
        onEdit = onEdit,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer),
    )
}