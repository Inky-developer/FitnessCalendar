package com.inky.fitnesscalendar.ui.views

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.Activity
import com.inky.fitnesscalendar.data.ActivityFilter
import com.inky.fitnesscalendar.ui.components.ActivityCard
import com.inky.fitnesscalendar.ui.components.NewActivityFAB
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.ui.util.sharedElement
import com.inky.fitnesscalendar.view_model.ActivityLogViewModel
import kotlinx.coroutines.launch

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
) {
    val scope = rememberCoroutineScope()
    val activityListState = rememberLazyListState()
    val activities by remember(filter) { viewModel.repository.getActivities(filter) }
        .collectAsState(initial = emptyList())

    // Scroll to requested activity or to the newest activity
    var scrollToId by remember { mutableStateOf<Int?>(null) }
    var latestActivity by remember { mutableStateOf(activities.firstOrNull()) }
    LaunchedEffect(activities) {
        if (scrollToId != null) {
            val index = activities.withIndex().find { it.value.uid == scrollToId }?.index
            if (index != null) {
                activityListState.animateScrollToItem(index)
            }
            scrollToId = null
        } else if (activities.firstOrNull()?.uid != latestActivity?.uid) {
            activityListState.animateScrollToItem(0)
        }
        latestActivity = activities.firstOrNull()
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.activity_log)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
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
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            AnimatedVisibility(visible = !filter.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(all = 8.dp)
                ) {
                    FilterInformation(filter = filter, onChange = onEditFilter)
                }
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
                    items(activities, key = { it.uid ?: -1 }) { activity ->
                        ActivityCard(
                            activity,
                            onDelete = {
                                scope.launch { viewModel.repository.deleteActivity(activity) }
                            },
                            onJumpTo = if (!filter.isEmpty()) {
                                {
                                    onEditFilter(ActivityFilter())
                                    scrollToId = activity.uid
                                }
                            } else null,
                            onEdit = onEditActivity,
                            localizationRepository = viewModel.repository.localizationRepository,
                            modifier = Modifier
                                .animateItem(fadeInSpec = null, fadeOutSpec = null)
                                .sharedElement(SharedContentKey.ActivityCard(activity.uid))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterInformation(filter: ActivityFilter, onChange: (ActivityFilter) -> Unit) {
    val listState = rememberLazyListState()
    LazyRow(state = listState) {
        if (filter.text != null) {
            item {
                FilterChip(
                    onClick = { onChange(filter.copy(text = null)) },
                    label = { Text(filter.text) },
                    leadingIcon = { Icon(Icons.Outlined.Edit, stringResource(R.string.text)) }
                )
            }
        }
        if (filter.range != null) {
            item {
                FilterChip(
                    onClick = { onChange(filter.copy(range = null)) },
                    label = { Text(stringResource(filter.range.nameId)) },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.DateRange,
                            stringResource(R.string.date_range)
                        )
                    }
                )
            }
        }
        items(filter.categories) { category ->
            FilterChip(
                onClick = { onChange(filter.copy(categories = filter.categories.filter { it != category })) },
                leadingIcon = { Text(category.emoji, style = MaterialTheme.typography.titleLarge) },
                label = {
                    Text(
                        stringResource(category.nameId),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
        items(filter.types) { type ->
            FilterChip(
                onClick = { onChange(filter.copy(types = filter.types.filter { it != type })) },
                leadingIcon = { Text(type.emoji, style = MaterialTheme.typography.titleLarge) },
                label = {
                    Text(
                        stringResource(type.nameId),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
            )
        }
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
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .animateItem()
    )
}