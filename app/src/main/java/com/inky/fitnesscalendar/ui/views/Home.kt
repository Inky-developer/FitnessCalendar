package com.inky.fitnesscalendar.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityFilter
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.ui.components.CompactActivityCard
import com.inky.fitnesscalendar.ui.components.NewActivityFAB
import com.inky.fitnesscalendar.util.Duration
import com.inky.fitnesscalendar.util.Duration.Companion.until
import com.inky.fitnesscalendar.view_model.TodayViewModel
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    viewModel: TodayViewModel = hiltViewModel(),
    isNewActivityOpen: Boolean,
    onNewActivity: () -> Unit,
    onNavigateActivity: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = stringResource(R.string.Menu),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            NewActivityFAB(onClick = onNewActivity, menuOpen = isNewActivityOpen)
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            val filterThisWeek = remember {
                ActivityFilter(
                    startRangeDate = Date.from(
                        Instant.ofEpochMilli(
                            Instant.now().toEpochMilli() - ChronoUnit.WEEKS.duration.toMillis() * 7
                        )
                    )
                )
            }
            val weeklyActivities by viewModel.repository.getActivities(filterThisWeek)
                .collectAsState(initial = emptyList())
            val weeklyStats by remember {
                derivedStateOf { ActivityStatistics(weeklyActivities) }
            }
            Statistics(stringResource(R.string.this_week), weeklyStats)

            val filterThisMonth = remember {
                ActivityFilter(
                    startRangeDate = Date.from(
                        Instant.ofEpochMilli(
                            Instant.now().toEpochMilli() - ChronoUnit.MONTHS.duration.toMillis() * 1
                        )
                    )
                )
            }
            val monthlyActivities by viewModel.repository.getActivities(filterThisWeek)
                .collectAsState(initial = emptyList())
            val monthlyStats by remember {
                derivedStateOf { ActivityStatistics(monthlyActivities) }
            }
            Statistics(stringResource(R.string.this_month), monthlyStats)

            ActivitiesToday(
                repository = viewModel.repository,
                onNavigateActivity = onNavigateActivity
            )
        }
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
        for ((activityClass, activities) in stats.activitiesByClass) {
            if (activities.isEmpty()) {
                continue
            }

            val durationString by remember {
                derivedStateOf {
                    Duration(activities.map { it.startTime until it.endTime }
                        .sumOf { it.elapsedMs }).format()

                }
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
                        R.plurals.num_activities,
                        activities.size,
                        activities.size
                    ),
                    modifier = Modifier.weight(1f)
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
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivitiesToday(repository: AppRepository, onNavigateActivity: () -> Unit) {
    val filterToday = remember { ActivityFilter.atDay(Instant.now()) }
    val activitiesToday by repository.getActivities(filterToday)
        .collectAsState(initial = emptyList())
    val isEmpty by remember {
        derivedStateOf {
            activitiesToday.isEmpty()
        }
    }

    AnimatedVisibility(visible = !isEmpty) {
        Card(
            onClick = onNavigateActivity,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            modifier = Modifier
                .padding(all = 8.dp)
                .fillMaxWidth()
        ) {
            LazyColumn {
                stickyHeader {
                    Text(
                        stringResource(R.string.today),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                items(activitiesToday, key = { it.uid ?: -1 }) { activity ->
                    CompactActivityCard(
                        activity = activity,
                        localizationRepository = repository.localizationRepository
                    )
                }
            }
        }
    }
}