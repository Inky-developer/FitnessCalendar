package com.inky.fitnesscalendar.ui.views

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilterChip
import com.inky.fitnesscalendar.ui.components.ActivityCard
import com.inky.fitnesscalendar.ui.components.NewActivityFAB
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.ui.util.sharedElement
import com.inky.fitnesscalendar.view_model.ActivityLogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLog(
    viewModel: ActivityLogViewModel = hiltViewModel(),
    filter: ActivityFilter,
    isNewActivityOpen: Boolean,
    onEditFilter: (ActivityFilter) -> Unit,
    onOpenDrawer: () -> Unit,
    onNewActivity: () -> Unit,
    onEditActivity: (Activity) -> Unit,
    onFilter: () -> Unit,
    initialSelectedActivityId: Int? = null,
) {
    val activityListState = rememberLazyListState()
    val activities by remember(filter) { viewModel.repository.getActivities(filter) }
        .collectAsState(initial = emptyList())
    val filterHistoryItems by viewModel.filterHistory.collectAsState(initial = emptyList())

    // Scroll to requested activity or to the newest activity
    var scrollToId by remember(initialSelectedActivityId) { mutableStateOf(initialSelectedActivityId) }
    var latestActivity by remember { mutableStateOf(activities.firstOrNull()?.activity) }
    LaunchedEffect(activities) {
        if (scrollToId != null) {
            val index = activities.withIndex().find { it.value.activity.uid == scrollToId }?.index
            if (index != null) {
                activityListState.animateScrollToItem(index)
                scrollToId = null
            }
        } else if (activities.firstOrNull()?.activity?.uid != latestActivity?.uid) {
            activityListState.animateScrollToItem(0)
        }
        latestActivity = activities.firstOrNull()?.activity
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val topAppBarColors = TopAppBarDefaults.topAppBarColors(
        scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )

    val colorTransitionFraction = scrollBehavior.state.overlappedFraction
    val fraction = if (colorTransitionFraction > 0.01f) 1f else 0f
    val appBarContainerColor by animateColorAsState(
        targetValue = lerp(
            topAppBarColors.containerColor,
            topAppBarColors.scrolledContainerColor,
            FastOutLinearInEasing.transform(fraction)
        ),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

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
            NewActivityFAB(
                onClick = {
                    onNewActivity()
                },
                menuOpen = isNewActivityOpen,
            )
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
            if (activities.isEmpty()) {
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
                LazyColumn(
                    state = activityListState,
                    contentPadding = PaddingValues(bottom = 128.dp),
                ) {
                    items(activities, key = { it.activity.uid ?: -1 }) { typeActivity ->
                        ActivityCard(
                            typeActivity,
                            onDelete = {
                                viewModel.deleteActivity(typeActivity.activity)
                            },
                            onJumpTo = if (!filter.isEmpty()) {
                                {
                                    onEditFilter(ActivityFilter())
                                    scrollToId = typeActivity.activity.uid
                                }
                            } else null,
                            onFilter = if (filter.isEmpty()) {
                                onEditFilter
                            } else null,
                            onEdit = onEditActivity,
                            localizationRepository = viewModel.repository.localizationRepository,
                            modifier = Modifier
                                .animateItem(fadeInSpec = null, fadeOutSpec = null)
                                .sharedElement(SharedContentKey.ActivityCard(typeActivity.activity.uid))
                        )
                    }
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