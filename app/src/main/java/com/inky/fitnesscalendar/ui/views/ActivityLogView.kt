package com.inky.fitnesscalendar.ui.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.components.ActivityCard
import com.inky.fitnesscalendar.ui.components.ActivityCardCallbacks
import com.inky.fitnesscalendar.ui.components.ActivityImage
import com.inky.fitnesscalendar.ui.components.FilterInformation
import com.inky.fitnesscalendar.ui.components.NewActivityFAB
import com.inky.fitnesscalendar.ui.components.NoActivitiesInfoBox
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.components.getAppBarContainerColor
import com.inky.fitnesscalendar.ui.util.Icons
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.ui.util.sharedElement
import com.inky.fitnesscalendar.view_model.ActivityLogViewModel
import com.inky.fitnesscalendar.view_model.activity_log.ActivityListItem
import com.inky.fitnesscalendar.view_model.activity_log.ActivityListState
import kotlinx.coroutines.launch

@Composable
fun ActivityLog(
    viewModel: ActivityLogViewModel = hiltViewModel(),
    filter: ActivityFilter,
    activityCardCallbacks: ActivityCardCallbacks,
    onEditFilter: (ActivityFilter) -> Unit,
    onOpenDrawer: () -> Unit,
    onNewActivity: () -> Unit,
    onShowDay: (EpochDay) -> Unit,
    onFilter: () -> Unit,
    onSummary: () -> Unit,
    initialSelectedActivityId: Int? = null,
) {

    val activityListState by viewModel.activityListState.collectAsState()
    LaunchedEffect(filter) {
        viewModel.setFilter(filter)
    }

    ActivityLogImpl(
        state = activityListState,
        snackbarHostState = viewModel.snackbarHostState,
        localizationRepository = viewModel.repository.localizationRepository,
        activityCardCallbacks = activityCardCallbacks,
        onEditFilter = onEditFilter,
        onOpenDrawer = onOpenDrawer,
        onNewActivity = onNewActivity,
        onShowDay = onShowDay,
        onFilter = onFilter,
        onSummary = onSummary,
        initialSelectedActivityId = initialSelectedActivityId
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityLogImpl(
    state: ActivityListState,
    snackbarHostState: SnackbarHostState,
    localizationRepository: LocalizationRepository,
    activityCardCallbacks: ActivityCardCallbacks,
    onEditFilter: (ActivityFilter) -> Unit,
    onOpenDrawer: () -> Unit,
    onNewActivity: () -> Unit,
    onShowDay: (EpochDay) -> Unit,
    onFilter: () -> Unit,
    onSummary: () -> Unit,
    initialSelectedActivityId: Int?,
) {
    val scope = rememberCoroutineScope()
    val isAtTopOfList by remember { derivedStateOf { state.listState.firstVisibleItemIndex <= 1 } }

    // Scroll to requested activity or to the newest activity
    var nextScrollTarget by rememberSaveable(initialSelectedActivityId) {
        mutableStateOf(initialSelectedActivityId)
    }
    LaunchedEffect(state.data) {
        if (state.data != null) {
            if (nextScrollTarget != null) {
                state.scrollToActivity(nextScrollTarget)
                nextScrollTarget = null
            } else if (state.listState.firstVisibleItemIndex == 0) {
                state.listState.scrollToItem(1)
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val appBarContainerColor = getAppBarContainerColor(scrollBehavior = scrollBehavior)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.activity_log)) },
                colors = defaultTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icons.Menu(contentDescription = stringResource(R.string.Menu))
                    }
                },
                actions = {
                    IconButton(onClick = onSummary) {
                        Icon(
                            painterResource(R.drawable.outline_assessment_24),
                            stringResource(R.string.summary)
                        )
                    }
                    IconButton(onClick = onFilter) {
                        val icon = if (state.filter.isEmpty()) Icons.FilterOff else Icons.FilterOn
                        icon(stringResource(R.string.filter))
                    }
                },
                scrollBehavior = scrollBehavior,
                modifier = Modifier.sharedBounds(SharedContentKey.AppBar)
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedVisibility(
                    visible = !isAtTopOfList,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    SmallFloatingActionButton(onClick = {
                        scope.launch {
                            state.listState.animateScrollToItem(1)
                            scrollBehavior.state.contentOffset = 0f
                            scrollBehavior.state.heightOffset = 0f
                        }
                    }) {
                        Icons.KeyboardArrowUp(stringResource(R.string.scroll_to_top))
                    }
                }
                NewActivityFAB(onClick = { onNewActivity() })
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(appBarContainerColor)
                    .padding(all = 8.dp)
            ) {
                FilterInformation(filter = state.filter, onChange = onEditFilter)
            }

            AnimatedContent(state.data, label = "EmptyStateAnimation") { data ->
                when {
                    data == null -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }

                    data.numActivities == 0 -> NoActivitiesInfoBox(
                        state.filter.isEmpty(),
                        modifier = Modifier.fillMaxSize()
                    )

                    else -> ActivityList(
                        state = state,
                        data = data,
                        activityCardCallbacks = activityCardCallbacks.copy(onJumpTo = null),
                        localizationRepository = localizationRepository,
                        onShowDay = onShowDay,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActivityList(
    state: ActivityListState,
    data: ActivityListState.Data,
    activityCardCallbacks: ActivityCardCallbacks,
    localizationRepository: LocalizationRepository,
    onShowDay: (EpochDay) -> Unit,
) {
    val listItems = data.items
    val listState = state.listState

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 128.dp),
    ) {
        item(contentType = -1) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(all = 8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    pluralStringResource(
                        R.plurals.num_activities,
                        data.numActivities,
                        data.numActivities
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        for (item in listItems) {
            when (item) {
                is ActivityListItem.DateHeader -> stickyHeader(
                    key = item.day.day,
                    contentType = item.contentType
                ) {
                    DayRow(
                        item.day,
                        onShowDay = { onShowDay(item.day.day) },
                        modifier = Modifier.animateItem()
                    )
                }

                is ActivityListItem.Activity -> item(
                    key = item.richActivity.activity.uid ?: -1,
                    contentType = item.contentType
                ) {
                    ActivityCard(
                        item.richActivity,
                        callbacks = activityCardCallbacks,
                        localizationRepository = localizationRepository,
                        modifier = Modifier
                            .animateItem()
                            .sharedElement(SharedContentKey.ActivityCard(item.richActivity.activity.uid))
                    )
                }
            }
        }
    }
}

@Composable
private fun DayRow(day: Day, onShowDay: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp)
            .fillMaxWidth()
            .clickable { onShowDay() }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                LocalizationRepository.localDateFormatter.format(day.day.toLocalDate()),
                style = MaterialTheme.typography.titleMedium,
            )

            if (day.feel != Feel.Ok) {
                Text(day.feel.emoji, style = MaterialTheme.typography.titleMedium)
            }
        }

        if (day.description.isNotBlank()) {
            Text(
                day.description,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(all = 8.dp)
            )
        }

        if (day.imageName != null) {
            ActivityImage(
                uri = day.imageName.getImageUri(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        if (day.description.isNotBlank() || day.imageName != null) {
            HorizontalDivider()
        }
    }
}

