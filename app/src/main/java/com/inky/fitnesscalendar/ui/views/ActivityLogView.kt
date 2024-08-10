package com.inky.fitnesscalendar.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilterChip
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.ui.components.ActivityCard
import com.inky.fitnesscalendar.ui.components.NewActivityFAB
import com.inky.fitnesscalendar.ui.components.PlaceIcon
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.getAppBarContainerColor
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.ui.util.sharedElement
import com.inky.fitnesscalendar.view_model.ActivityLogViewModel
import com.inky.fitnesscalendar.view_model.activity_log.ActivityListItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLog(
    viewModel: ActivityLogViewModel = hiltViewModel(),
    filter: ActivityFilter,
    onEditFilter: (ActivityFilter) -> Unit,
    onOpenDrawer: () -> Unit,
    onNewActivity: () -> Unit,
    onEditActivity: (Activity) -> Unit,
    onFilter: () -> Unit,
    initialSelectedActivityId: Int? = null,
) {
    val scope = rememberCoroutineScope()

    val activityListState by viewModel.activityListState
    val isAtTopOfList by remember { derivedStateOf { activityListState.firstVisibleItemIndex <= 1 } }

    LaunchedEffect(filter) {
        viewModel.setFilter(filter)
    }

    val activities by viewModel.activities.collectAsState()
    val activityListItems by viewModel.activityListItems.collectAsState(initial = emptyList())
    val activitiesEmpty by remember { derivedStateOf { activities.isEmpty() } }

    val days by viewModel.days.collectAsState()

    val filterHistoryItems by viewModel.filterHistory.collectAsState(initial = emptyList())

    // Scroll to requested activity or to the newest activity
    var scrollToId by remember(initialSelectedActivityId) { mutableStateOf(initialSelectedActivityId) }
    var latestActivity by remember { mutableStateOf(activities.firstOrNull()?.activity) }
    LaunchedEffect(activities) {
        if (scrollToId != null) {
            val index =
                activityListItems
                    .withIndex()
                    .firstOrNull { (_, item) -> item is ActivityListItem.Activity && item.richActivity.activity.uid == scrollToId }
                    ?.index
            if (index != null) {
                activityListState.animateScrollToItem(index + 1)
                scrollToId = null
            }
        } else if (activities.firstOrNull()?.activity?.uid != latestActivity?.uid) {
            activityListState.animateScrollToItem(1)
        }
        latestActivity = activities.firstOrNull()?.activity
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val topAppBarColors = TopAppBarDefaults.topAppBarColors(
        scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    val appBarContainerColor =
        getAppBarContainerColor(scrollBehavior = scrollBehavior, topAppBarColors = topAppBarColors)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.activity_log)) },
                colors = topAppBarColors,
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = stringResource(R.string.Menu),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onFilter() }) {
                        Icon(Icons.Outlined.Search, stringResource(R.string.filter))
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
                            activityListState.animateScrollToItem(1)
                            scrollBehavior.state.contentOffset = 0f
                            scrollBehavior.state.heightOffset = 0f
                        }
                    }) {
                        Icon(
                            Icons.Outlined.KeyboardArrowUp,
                            stringResource(R.string.scroll_to_top)
                        )
                    }
                }
                NewActivityFAB(onClick = { onNewActivity() })
            }
        },
        snackbarHost = { SnackbarHost(hostState = viewModel.snackbarHostState) },
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
                FilterInformation(
                    filter = filter,
                    historyItems = filterHistoryItems,
                    onChange = onEditFilter
                )
            }
            if (activitiesEmpty) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(vertical = 8.dp)
                        .fillMaxSize()
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        stringResource(R.string.info),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .width(32.dp)
                            .aspectRatio(1f)
                            .align(Alignment.CenterVertically)
                    )
                    val textId =
                        if (filter.isEmpty()) R.string.no_activities_yet else R.string.no_activities_with_filter
                    Text(
                        stringResource(textId),
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            } else {
                ActivityList(
                    listState = activityListState,
                    activityListItems = activityListItems,
                    numActivities = activities.size,
                    days = days,
                    filter = filter,
                    localizationRepository = viewModel.repository.localizationRepository,
                    onJumpToActivity = {
                        onEditFilter(ActivityFilter())
                        scrollToId = it.uid
                    },
                    onEditFilter = onEditFilter,
                    onEditActivity = onEditActivity,
                    onDeleteActivity = { viewModel.deleteActivity(it) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActivityList(
    listState: LazyListState,
    activityListItems: List<ActivityListItem>,
    numActivities: Int,
    days: Map<EpochDay, Day>,
    filter: ActivityFilter,
    localizationRepository: LocalizationRepository,
    onJumpToActivity: (Activity) -> Unit,
    onEditFilter: (ActivityFilter) -> Unit,
    onEditActivity: (Activity) -> Unit,
    onDeleteActivity: (RichActivity) -> Unit
) {
    val isFilterEmpty = remember(filter) { filter.isEmpty() }

    LaunchedEffect(key1 = null) {
        listState.scrollToItem(1)
    }

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
                    pluralStringResource(R.plurals.num_activities, numActivities, numActivities),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        for ((index, item) in activityListItems.withIndex()) {
            when (item) {
                is ActivityListItem.DateHeader -> stickyHeader(
                    key = item.date,
                    contentType = item.contentType
                ) {
                    val feel = days[EpochDay(item.date.toEpochDay())]?.feel
                    val paddingTop = if (index == 0) 0.dp else 4.dp
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = paddingTop, bottom = 4.dp)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                            .animateItem()
                    ) {
                        Text(
                            LocalizationRepository.localDateFormatter.format(item.date),
                            style = MaterialTheme.typography.titleMedium,
                            color = contentColorFor(MaterialTheme.colorScheme.primary)
                        )

                        if (feel != null) {
                            Text(feel.emoji, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                is ActivityListItem.Activity -> item(
                    key = item.richActivity.activity.uid ?: -1,
                    contentType = item.contentType
                ) {
                    ActivityCard(
                        item.richActivity,
                        onDelete = {
                            onDeleteActivity(item.richActivity)
                        },
                        onFilter = if (isFilterEmpty) {
                            onEditFilter
                        } else null,
                        onJumpTo = if (!isFilterEmpty) {
                            { onJumpToActivity(item.richActivity.activity) }
                        } else null,
                        onEdit = onEditActivity,
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
private fun FilterInformation(
    filter: ActivityFilter,
    historyItems: List<ActivityFilterChip>,
    onChange: (ActivityFilter) -> Unit
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val filterItems = remember(filter) { filter.items() }
    val filteredHistoryItems = remember(filter, historyItems) {
        historyItems.filter {
            !filterItems.contains(
                it
            )
        }
    }

    LazyRow(state = listState) {
        items(filterItems) { chip ->
            FilterChip(
                onClick = { onChange(chip.removeFrom(filter)) },
                label = { Text(chip.displayText(context)) },
                leadingIcon = { FilterChipIcon(chip) }
            )
        }

        items(filteredHistoryItems) { chip ->
            SuggestionFilterChip(
                onClick = { onChange(chip.addTo(filter)) },
                label = { Text(chip.displayText(context)) },
            )
        }
    }
}

@Composable
private fun FilterChipIcon(chip: ActivityFilterChip) {
    when (chip) {
        is ActivityFilterChip.AttributeFilterChip -> Icon(
            painterResource(R.drawable.outline_label_24),
            stringResource(R.string.attribute)
        )

        is ActivityFilterChip.CategoryFilterChip -> Text(
            chip.category.emoji,
            style = MaterialTheme.typography.titleLarge
        )

        is ActivityFilterChip.DateFilterChip -> Icon(
            Icons.Outlined.DateRange,
            stringResource(R.string.date_range)
        )

        is ActivityFilterChip.TextFilterChip -> Icon(
            Icons.Outlined.Edit,
            stringResource(R.string.text)
        )

        is ActivityFilterChip.TypeFilterChip -> Text(
            chip.type.emoji,
            style = MaterialTheme.typography.titleLarge
        )

        is ActivityFilterChip.PlaceFilterChip -> PlaceIcon(chip.place)
    }
}

@Composable
private fun LazyItemScope.FilterChip(
    leadingIcon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    onClick: () -> Unit
) {
    InputChip(
        selected = false,
        onClick = onClick,
        label = label,
        leadingIcon = leadingIcon,
        trailingIcon = { Icon(Icons.Outlined.Clear, stringResource(R.string.clear)) },
        colors = InputChipDefaults.inputChipColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .animateItem()
    )
}

@Composable
private fun LazyItemScope.SuggestionFilterChip(
    label: @Composable () -> Unit,
    onClick: () -> Unit
) {
    SuggestionChip(
        onClick = onClick,
        label = label,
        icon = {
            Icon(
                painterResource(
                    R.drawable.outline_history_24,
                ),
                stringResource(R.string.recent)
            )
        },
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .animateItem()
    )
}