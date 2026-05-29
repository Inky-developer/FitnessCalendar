package com.inky.fitnesscalendar.ui.views

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.db.entities.Recording
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.db.entities.RichRecording
import com.inky.fitnesscalendar.di.AppRepository
import com.inky.fitnesscalendar.repository.LocalizationRepository
import com.inky.fitnesscalendar.ui.components.ActivityCard
import com.inky.fitnesscalendar.ui.components.ActivityCardCallbacks
import com.inky.fitnesscalendar.ui.components.ActivityImage
import com.inky.fitnesscalendar.ui.components.CompactActivityCard
import com.inky.fitnesscalendar.ui.components.DescriptionTextInput
import com.inky.fitnesscalendar.ui.components.FeelSelector
import com.inky.fitnesscalendar.ui.components.ImageViewer
import com.inky.fitnesscalendar.ui.components.NewActivityFAB
import com.inky.fitnesscalendar.ui.components.Timer
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.util.Icons
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.horizontalOrderedTransitionSpec
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.ui.util.sharedElement
import com.inky.fitnesscalendar.util.showRecordingNotification
import com.inky.fitnesscalendar.view_model.HomeViewModel
import com.inky.fitnesscalendar.view_model.statistics.Period
import kotlinx.coroutines.launch

private const val TAG = "HOME"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
context(app: AppRepository)
fun Home(
    viewModel: HomeViewModel = hiltViewModel(),
    activityCardCallbacks: ActivityCardCallbacks,
    onNewActivity: () -> Unit,
    onEditDay: (EpochDay) -> Unit,
    onRecordActivity: () -> Unit,
    onNavigateToday: () -> Unit,
    onNavigateStats: (Period) -> Unit,
    onOpenDrawer: () -> Unit
) {
    val weeklyStats by viewModel.weekStats.collectAsState(initial = null)
    val monthlyStats by viewModel.monthStats.collectAsState(initial = null)
    val activitiesToday by viewModel.activitiesToday.collectAsState(initial = null)
    val day by viewModel.today.collectAsState()
    val recentActivity by viewModel.mostRecentActivity.collectAsState(initial = null)
    val typeRecordings by viewModel.recordings.collectAsState(initial = null)
    val scrollState = rememberScrollState()

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
                title = { Text(stringResource(R.string.app_name)) },
                colors = defaultTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icons.Menu(contentDescription = stringResource(R.string.Menu))
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
                        Icons.PlayArrowFilled(stringResource(R.string.action_record))
                    }
                },
                scrollBehavior = scrollBehavior,
                modifier = Modifier.sharedBounds(SharedContentKey.AppBar)
            )
        },
        floatingActionButton = { NewActivityFAB(onClick = onNewActivity) },
        snackbarHost = { SnackbarHost(app.snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(bottom = 64.dp) // Account for fab, maybe this should be set dynamically?
        ) {
            AnimatedVisibility(visible = typeRecordings?.isNotEmpty() ?: false) {
                Recordings(
                    richRecordings = typeRecordings ?: emptyList(),
                    onAbort = { viewModel.abortRecording(it) },
                    onSave = { viewModel.saveRecording(it) },
                    onUpdate = { viewModel.updateRecording(it) }
                )
            }

            RecentActivityOrNull(
                recentActivity,
                callbacks = activityCardCallbacks.copy(onFilterByType = null),
            )
            Today(
                richActivities = activitiesToday ?: emptyList(),
                day = day,
                onDay = { viewModel.updateDay(it) },
                onEditDay = onEditDay,
                onNavigateToday = onNavigateToday
            )
            StatisticsIfNotNull(
                stringResource(R.string.last_seven_days),
                weeklyStats,
                onClick = { onNavigateStats(Period.Day) }
            )
            StatisticsIfNotNull(
                stringResource(R.string.four_weeks),
                monthlyStats,
                onClick = { onNavigateStats(Period.Week) }
            )
        }
    }
}

@Composable
context(_: LocalizationRepository)
fun Recordings(
    richRecordings: List<RichRecording>,
    onAbort: (Recording) -> Unit,
    onUpdate: suspend (Recording) -> Unit,
    onSave: (Recording) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
    ) {
        for (typeRecording in richRecordings) {
            Timer { time ->
                RecordingStatus(
                    typeRecording,
                    time,
                    onAbort = { onAbort(typeRecording.recording) },
                    onSave = { onSave(typeRecording.recording) },
                    onUpdate = onUpdate,
                )
            }
        }
    }
}

@Composable
context(localizationRepository: LocalizationRepository)
fun RecordingStatus(
    richRecording: RichRecording,
    currentTimeMs: Long,
    onUpdate: suspend (Recording) -> Unit,
    onAbort: () -> Unit,
    onSave: () -> Unit
) {
    val timeString =
        remember { localizationRepository.formatRelativeDate(richRecording.recording.startTime) }

    val durationString = remember(currentTimeMs) {
        localizationRepository.formatDuration(richRecording.recording.startTime)
    }

    val recording = richRecording.recording
    var updatedDescription by rememberSaveable() { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    updatedDescription?.let { description ->
        Dialog(onDismissRequest = {
            scope.launch {
                onUpdate(recording.copy(description = description))
                updatedDescription = null
            }
        }) {
            DescriptionTextInput(
                initialDescription = description,
                onDescription = { updatedDescription = it },
                autoShowKeyboard = true,
            )
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
                    richRecording.type.name,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Text(timeString)
            Text(durationString, modifier = Modifier)
        }
        AnimatedContent(recording.description) { description ->
            if (description.isNotBlank()) {
                Row {
                    Text(
                        description,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(4.dp)
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { updatedDescription = recording.description }) {
                Icons.Edit(stringResource(R.string.edit_object))
            }
            Spacer(modifier = Modifier.weight(1f))
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
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
                    categoryStats.totalTime().format()
                }

                Row(
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        activityCategory.emoji + activityCategory.text(),
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
                Icons.Info(stringResource(R.string.info))
                Text(
                    stringResource(R.string.no_activities_in_this_period),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
context(_: LocalizationRepository)
fun Today(
    richActivities: List<RichActivity>,
    day: Day,
    onDay: (Day) -> Unit,
    onEditDay: (EpochDay) -> Unit,
    onNavigateToday: () -> Unit,
) {
    var showImageViewer by rememberSaveable { mutableStateOf(false) }

    Card(
        onClick = onNavigateToday,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
            .testTag("Today")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                stringResource(R.string.today),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.sharedBounds(SharedContentKey.DayTitle)
            )

            Spacer(modifier = Modifier.weight(1f))

            CompactFeelSelector(
                day.feel,
                onFeel = { onDay(day.copy(feel = it)) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            IconButton(
                onClick = { onEditDay(day.day) },
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icons.Edit(stringResource(R.string.edit_day))
            }
        }

        AnimatedContent(
            targetState = day.imageName?.getImageUri(),
            label = stringResource(R.string.image)
        ) { imageUri ->
            if (imageUri != null) {
                ActivityImage(
                    uri = imageUri,
                    onClick = { showImageViewer = true },
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .sharedElement(SharedContentKey.DayImage)
                )
            }
        }

        AnimatedContent(
            targetState = day.description,
            label = stringResource(R.string.description)
        ) { description ->
            if (description.isNotBlank()) {
                // OnSecondary is too bright in light mode
                val backgroundColor =
                    if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.surfaceContainer

                Text(
                    description,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(backgroundColor)
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth()
                        .sharedElement(SharedContentKey.DayDescription)
                )
            }
        }

        AnimatedContent(
            targetState = richActivities.isEmpty(),
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
                        Icons.Info(
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
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        for (richActivity in richActivities) {
                            CompactActivityCard(
                                richActivity = richActivity,
                                modifier = Modifier.sharedElement(
                                    SharedContentKey.ActivityCard(
                                        richActivity.activity.uid
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    val imageUri = day.imageName?.getImageUri()
    if (showImageViewer && imageUri != null) {
        ImageViewer(imageUri = imageUri, onDismiss = { showImageViewer = false })
    }
}

@Composable
fun CompactFeelSelector(feel: Feel, onFeel: (Feel) -> Unit, modifier: Modifier = Modifier) {
    val expanded =
        rememberSaveable(
            saver = listSaver(
                save = { listOf(it.targetState) },
                restore = { MutableTransitionState(it[0]) })
        ) { MutableTransitionState(false) }

    Column(modifier = modifier) {
        AnimatedContent(
            targetState = feel,
            transitionSpec = horizontalOrderedTransitionSpec(),
            label = stringResource(R.string.feel)
        ) { actualFeel ->
            OutlinedButton(
                onClick = { expanded.targetState = true },
                contentPadding = PaddingValues(all = 4.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Text(
                    actualFeel!!.emoji,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.sharedElement(SharedContentKey.DayFeel)
                )
            }
        }

        if (expanded.currentState || expanded.targetState) {
            Popup(
                alignment = Alignment.BottomStart,
                onDismissRequest = { expanded.targetState = false },
                properties = PopupProperties(focusable = true)
            ) {
                AnimatedVisibility(visibleState = expanded) {
                    FeelSelector(
                        feel = feel,
                        onChange = {
                            onFeel(it)
                            expanded.targetState = false
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.onSecondary)
                    )
                }
            }
        }
    }
}

@Composable
context(_: LocalizationRepository)
fun RecentActivityOrNull(
    richActivity: RichActivity?,
    callbacks: ActivityCardCallbacks,
) {
    AnimatedContent(
        targetState = richActivity,
        label = stringResource(R.string.recent_activity)
    ) { actualActivity ->
        if (actualActivity != null) {
            RecentActivity(
                richActivity = actualActivity,
                callbacks = callbacks,
            )
        }
    }
}

@Composable
context(_: LocalizationRepository)
fun RecentActivity(
    richActivity: RichActivity,
    callbacks: ActivityCardCallbacks,
) {
    ActivityCard(
        richActivity = richActivity,
        callbacks = callbacks,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = contentColorFor(MaterialTheme.colorScheme.tertiaryContainer),
    )
}