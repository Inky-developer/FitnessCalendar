package com.inky.fitnesscalendar.ui.views

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.Activity
import com.inky.fitnesscalendar.data.ActivityFilter
import com.inky.fitnesscalendar.ui.components.ActivityCard
import com.inky.fitnesscalendar.view_model.TodayViewModel
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun Today(
    viewModel: TodayViewModel = hiltViewModel(),
    filter: ActivityFilter,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    isNewActivityOpen: Boolean,
    onNewActivity: () -> Unit,
    onEditActivity: (Activity) -> Unit,
    onFilter: () -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val activityListState = rememberLazyListState()
    val activities by viewModel.repository.getActivities(filter)
        .collectAsState(initial = emptyList())

    // Detect when a new activity was inserted and scroll to it
    var latestActivity by remember { mutableStateOf(activities.firstOrNull()) }
    LaunchedEffect(activities) {
        if (activities.firstOrNull()?.uid != latestActivity?.uid) {
            activityListState.animateScrollToItem(0)
        }
        latestActivity = activities.firstOrNull()
    }

    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.primaryContainer) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.ic_launcher_foreground),
                    stringResource(R.string.app_icon)
                )
                Text(
                    stringResource(id = R.string.app_name),
                    modifier = Modifier.padding(all = 16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = TextStyle(
                        fontSize = 26.sp,
                        shadow = Shadow(color = MaterialTheme.colorScheme.primary, blurRadius = 4f),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                    )
                )
            }
            HorizontalDivider()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.today)) },
                    selected = true,
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .padding(top = 8.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.settings)) },
                    onClick = { /*TODO*/ },
                    selected = false,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }) {
        Scaffold(topBar = {
            with(sharedTransitionScope) {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.today)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            }
                        }) {
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
                    modifier = Modifier.sharedBounds(
                        rememberSharedContentState(key = "appBar"),
                        animatedVisibilityScope = animatedContentScope
                    )
                )
            }
        }, floatingActionButton = {
            NewActivityFAB(onClick = {
                onNewActivity()
            }, menuOpen = isNewActivityOpen)
        }) { innerPadding ->
            LazyColumn(
                state = activityListState,
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(bottom = 128.dp),
                reverseLayout = false
            ) {
                items(activities, key = { it.uid ?: -1 }) { activity ->
                    ActivityCard(
                        activity,
                        onDelete = {
                            scope.launch { viewModel.repository.deleteActivity(activity) }
                        },
                        onEdit = onEditActivity,
                        localizationRepository = viewModel.repository.localizationRepository,
                        modifier = Modifier.animateItemPlacement()
                    )
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
            }
        }
    }
}

@Composable
fun NewActivityFAB(onClick: () -> Unit, menuOpen: Boolean) {
    val angle = animateFloatAsState(
        targetValue = if (menuOpen) {
            45f
        } else {
            0f
        }, label = "fab_rotation"
    )
    FloatingActionButton(
        onClick = onClick, containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Icon(
            Icons.Filled.Add,
            stringResource(R.string.action_new_activity),
            modifier = Modifier.rotate(angle.value)
        )
    }
}