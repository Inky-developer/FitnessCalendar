package com.inky.fitnesscalendar.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.ui.components.NavigationDrawer
import com.inky.fitnesscalendar.ui.views.ActivityLog
import com.inky.fitnesscalendar.ui.views.EditDayDialog
import com.inky.fitnesscalendar.ui.views.FilterView
import com.inky.fitnesscalendar.ui.views.Home
import com.inky.fitnesscalendar.ui.views.ImportExport
import com.inky.fitnesscalendar.ui.views.NewActivity
import com.inky.fitnesscalendar.ui.views.RecordActivity
import com.inky.fitnesscalendar.ui.views.StatisticsView
import com.inky.fitnesscalendar.ui.views.Views
import com.inky.fitnesscalendar.ui.views.settingsDestination
import com.inky.fitnesscalendar.view_model.GenericViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun App(viewModel: GenericViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    var filterState by rememberSaveable { mutableStateOf(ActivityFilter()) }
    val navigationDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val openDrawer = {
        scope.launch {
            if (navigationDrawerState.isClosed) {
                navigationDrawerState.open()
            } else {
                navigationDrawerState.close()
            }
        }
        Unit
    }
    var currentView by rememberSaveable { mutableStateOf<Views?>(null) }
    var isNewActivityOpen by rememberSaveable { mutableStateOf(false) }

    val typeRows by viewModel.repository.getActivityTypeRows().collectAsState(initial = emptyList())

    val context = LocalContext.current

    NavigationDrawer(
        drawerState = navigationDrawerState,
        currentView = currentView,
        onNavigate = {
            navController.navigate(it.getPath()) {
                popUpTo(it.getPath()) {
                    inclusive = true
                }
            }
            scope.launch {
                navigationDrawerState.close()
            }
        }
    ) {
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = Views.Home.getPath(),
            ) {
                composable(Views.Home.pathTemplate()) {
                    currentView = Views.Home
                    ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                        Home(
                            isNewActivityOpen = isNewActivityOpen,
                            onNewActivity = { navController.navigate(Views.NewActivity.getPath(-1)) },
                            onEditActivity = {
                                navController.navigate(Views.NewActivity.getPath(it.uid ?: -1))
                            },
                            onEditDay = {
                                navController.navigate(Views.EditDay.getPath(it.day))
                            },
                            onRecordActivity = { navController.navigate(Views.RecordActivity.getPath()) },
                            onNavigateActivity = {
                                navController.navigate(Views.ActivityLog.getPath())
                            },
                            onNavigateStats = {
                                navController.navigate(Views.Statistics.getPath(it.toString()))
                            },
                            onOpenDrawer = openDrawer
                        )
                    }
                }
                dialog(
                    Views.EditDay.pathTemplate(),
                    arguments = Views.EditDay.navArgs()
                ) { backStackEntry ->
                    currentView = Views.EditDay
                    val epochDay =
                        backStackEntry.arguments!!.let { Views.Argument.EPOCH_DAY.extract(it) }
                    EditDayDialog(
                        epochDay = epochDay,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(
                    Views.ActivityLog.pathTemplate(),
                    arguments = Views.ActivityLog.navArgs()
                ) { backStackEntry ->
                    currentView = Views.ActivityLog
                    val activityId =
                        backStackEntry.arguments?.let { Views.Argument.ACTIVITY_ID.extract(it) }
                    ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                        ActivityLog(
                            initialSelectedActivityId = activityId,
                            filter = filterState,
                            onOpenDrawer = openDrawer,
                            onNewActivity = {
                                navController.navigate(Views.NewActivity.getPath(-1))
                            },
                            onEditActivity = {
                                navController.navigate(Views.NewActivity.getPath(it.uid ?: -1))
                            },
                            onFilter = {
                                navController.navigate(Views.FilterActivity.getPath())
                            },
                            onEditFilter = {
                                filterState = it
                                viewModel.addToFilterHistory(filterState)
                            },
                            isNewActivityOpen = isNewActivityOpen
                        )
                    }
                }
                composable(
                    Views.FilterActivity.pathTemplate(),
                    enterTransition = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
                    },
                    exitTransition = {
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down)
                    }
                ) {
                    currentView = Views.FilterActivity
                    ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                        FilterView(
                            initialFilter = filterState,
                            onFilterChange = {
                                filterState = it
                                viewModel.addToFilterHistory(filterState)
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
                dialog(
                    Views.NewActivity.pathTemplate(), arguments = Views.NewActivity.navArgs()
                ) { backStackEntry ->
                    currentView = Views.NewActivity
                    isNewActivityOpen = true
                    val activityId =
                        backStackEntry.arguments?.let { Views.Argument.ACTIVITY_ID.extract(it) }
                    NewActivity(
                        activityId,
                        onSave = {
                            scope.launch {
                                viewModel.repository.saveActivity(it)
                            }
                            isNewActivityOpen = false
                            navController.popBackStack()
                        },
                        onNavigateBack = {
                            isNewActivityOpen = false
                            navController.popBackStack()
                        }
                    )
                }
                dialog(
                    Views.RecordActivity.pathTemplate(), arguments = Views.RecordActivity.navArgs()
                ) {
                    currentView = Views.RecordActivity
                    RecordActivity(
                        onStart = {
                            scope.launch {
                                viewModel.repository.startRecording(it, context)
                            }
                            navController.popBackStack()
                        },
                        onNavigateBack = { navController.popBackStack() },
                        typeRows = typeRows
                    )
                }
                composable(Views.ImportExport.pathTemplate()) {
                    currentView = Views.ImportExport
                    ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                        ImportExport(
                            onOpenDrawer = openDrawer
                        )
                    }
                }
                composable(
                    Views.Statistics.pathTemplate(),
                    arguments = Views.Statistics.navArgs()
                ) { backStackEntry ->
                    currentView = Views.Statistics

                    val initialPeriod =
                        backStackEntry.arguments?.let { Views.Argument.INITIAL_PERIOD.extract(it) }

                    ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                        StatisticsView(
                            initialPeriod = initialPeriod,
                            onOpenDrawer = openDrawer,
                            onViewActivity = {
                                navController.navigate(Views.ActivityLog.getPath(it.uid ?: -1))
                            }
                        )
                    }
                }
                settingsDestination(
                    sharedContentScope = this@SharedTransitionLayout,
                    onOpenDrawer = openDrawer,
                    onNavigate = { navController.navigate(it) },
                    onBack = { navController.popBackStack() },
                    onOpen = { currentView = Views.Settings }
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
class SharedTransitionData(
    private val _sharedTransitionScope: SharedTransitionScope?,
    private val _animatedContentScope: AnimatedContentScope?
) {
    val sharedTransitionScope get() = _sharedTransitionScope!!
    val animatedContentScope get() = _animatedContentScope!!
}

@OptIn(ExperimentalSharedTransitionApi::class)
val localSharedTransition = staticCompositionLocalOf { SharedTransitionData(null, null) }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AnimatedContentScope.ProvideSharedContent(
    sharedContentScope: SharedTransitionScope,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        value = localSharedTransition provides SharedTransitionData(
            sharedContentScope,
            this
        )
    ) {
        content()
    }
}