package com.inky.fitnesscalendar.ui

import android.app.Activity
import android.content.Context
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.di.DecisionTrees
import com.inky.fitnesscalendar.ui.components.NavigationDrawer
import com.inky.fitnesscalendar.ui.util.ProvideDatabaseValues
import com.inky.fitnesscalendar.ui.util.localDatabaseValues
import com.inky.fitnesscalendar.ui.views.ActivityEditState
import com.inky.fitnesscalendar.ui.views.ActivityLog
import com.inky.fitnesscalendar.ui.views.ActivityShareView
import com.inky.fitnesscalendar.ui.views.DayView
import com.inky.fitnesscalendar.ui.views.EditDayDialog
import com.inky.fitnesscalendar.ui.views.FilterView
import com.inky.fitnesscalendar.ui.views.Home
import com.inky.fitnesscalendar.ui.views.MapView
import com.inky.fitnesscalendar.ui.views.NewActivity
import com.inky.fitnesscalendar.ui.views.RecordActivity
import com.inky.fitnesscalendar.ui.views.StatisticsView
import com.inky.fitnesscalendar.ui.views.SummaryView
import com.inky.fitnesscalendar.ui.views.TrackDetailsView
import com.inky.fitnesscalendar.ui.views.TrackGraphView
import com.inky.fitnesscalendar.ui.views.Views
import com.inky.fitnesscalendar.ui.views.settings.SettingsViews
import com.inky.fitnesscalendar.ui.views.settingsDestination
import com.inky.fitnesscalendar.view_model.AppViewModel
import com.inky.fitnesscalendar.view_model.BaseViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset


@Composable
fun App(viewModel: BaseViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

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

    ProvideDatabaseValues(repository = viewModel.repository) {
        NavigationDrawer(
            drawerState = navigationDrawerState,
            currentView = currentView,
            onNavigate = {
                navController.navigate(it) {
                    popUpTo(it) {
                        inclusive = true
                    }
                }
                scope.launch {
                    navigationDrawerState.close()
                }
            }
        ) {
            AppNavigation(
                navController = navController,
                openDrawer = openDrawer,
                onCurrentView = { currentView = it }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
private fun AppNavigation(
    viewModel: AppViewModel = hiltViewModel(),
    navController: NavHostController,
    openDrawer: () -> Unit,
    onCurrentView: (Views) -> Unit,
) {
    var filterState by rememberSaveable { mutableStateOf(ActivityFilter()) }

    val scope = rememberCoroutineScope()

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = Views.Home,
        ) {
            composable<Views.Home> {
                onCurrentView(Views.Home)
                ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                    Home(
                        onNewActivity = { navController.navigate(Views.NewActivity()) },
                        onEditActivity = {
                            navController.navigate(Views.NewActivity(it.uid))
                        },
                        onShareActivity = {
                            if (it.activity.uid != null) {
                                navController.navigate(Views.ShareActivity(it.activity.uid))
                            }
                        },
                        onEditDay = {
                            navController.navigate(Views.EditDay(it.day))
                        },
                        onTrackDetails = {
                            if (it.uid != null) {
                                navController.navigate(Views.TrackDetails(it.uid))
                            }
                        },
                        onRecordActivity = { navController.navigate(Views.RecordActivity) },
                        onNavigateToday = { navController.navigate(Views.DayView()) },
                        onNavigateStats = {
                            navController.navigate(Views.Statistics(it.toString()))
                        },
                        onOpenDrawer = openDrawer
                    )
                }
            }
            dialog<Views.EditDay> { backStackEntry ->
                val route: Views.EditDay = backStackEntry.toRoute()
                onCurrentView(route)
                EditDayDialog(
                    epochDay = route.epochDay,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Views.DayView> { backStackEntry ->
                val route: Views.DayView = backStackEntry.toRoute()
                onCurrentView(route)

                ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                    DayView(
                        initialEpochDay = route.epochDay,
                        onEditActivity = {
                            navController.navigate(Views.NewActivity(it.activity.uid))
                        },
                        onShareActivity = {
                            if (it.activity.uid != null) {
                                navController.navigate(Views.ShareActivity(it.activity.uid))
                            }
                        },
                        onTrackDetails = {
                            if (it.activity.uid != null) {
                                navController.navigate(Views.TrackDetails(it.activity.uid))
                            }
                        },
                        onJumpToActivity = {
                            navController.navigate(Views.ActivityLog(it.activity.uid))
                        },
                        onNewActivity = {
                            navController.navigate(Views.NewActivity(rawInitialStartDay = it.day))
                        },
                        onEditDay = { navController.navigate(Views.EditDay(it.day)) },
                        onOpenDrawer = openDrawer
                    )
                }
            }
            composable<Views.ActivityLog> { backStackEntry ->
                val route: Views.ActivityLog = backStackEntry.toRoute()
                onCurrentView(route)
                ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                    ActivityLog(
                        initialSelectedActivityId = route.activityId,
                        filter = filterState,
                        onOpenDrawer = openDrawer,
                        onNewActivity = { navController.navigate(Views.NewActivity()) },
                        onEditActivity = {
                            navController.navigate(Views.NewActivity(it.uid))
                        },
                        onTrackDetails = {
                            if (it.uid != null) {
                                navController.navigate(Views.TrackDetails(it.uid))
                            }
                        },
                        onShowDay = { navController.navigate(Views.DayView(it.day)) },
                        onFilter = {
                            navController.navigate(Views.FilterActivity)
                        },
                        onSummary = {
                            navController.navigate(Views.SummaryView)
                        },
                        onShareActivity = {
                            if (it.uid != null) {
                                navController.navigate(Views.ShareActivity(it.uid))
                            }
                        },
                        onEditFilter = {
                            filterState = it
                            viewModel.addToFilterHistory(filterState)
                        },
                    )
                }
            }
            composable<Views.FilterActivity>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) }
            ) {
                onCurrentView(Views.FilterActivity)
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
            composable<Views.ShareActivity>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) }
            ) {
                onCurrentView(Views.ShareActivity(-1))
                val route: Views.ShareActivity = it.toRoute()

                ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                    ActivityShareView(
                        activityId = route.activityId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable<Views.NewActivity>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) }
            ) { backStackEntry ->
                val route: Views.NewActivity = backStackEntry.toRoute()
                onCurrentView(route)
                NewActivity(
                    route.activityId,
                    onSave = {
                        scope.launch {
                            viewModel.repository.saveActivity(it)
                        }
                        navController.popBackStack()
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateNewPlace = { navController.navigate(SettingsViews.PlaceDialog()) },
                    initialDay = route.initialStartDay
                )
            }

            composable<Views.ApiNewActivity>(
                deepLinks = listOf(navDeepLink<Views.ApiNewActivity>(basePath = Views.ApiNewActivity.DEEP_LINK_BASE_URL))
            ) { backStackEntry ->
                val route: Views.ApiNewActivity = backStackEntry.toRoute()

                val context = LocalContext.current
                val activityTypes = localDatabaseValues.current.activityTypes
                val initialState = remember(route) {
                    val prediction = DecisionTrees.classifyNow(context)
                    val activityType =
                        route.activityTypeId?.let { id -> activityTypes.find { it.uid == id } }
                    val start =
                        route.startTime?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
                    val end =
                        route.endTime?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }

                    ActivityEditState(activity = null, prediction = prediction).let { state ->
                        val newActivitySelectorState = state.activitySelectorState.copy(
                            activityType = activityType ?: state.activitySelectorState.activityType
                        )
                        state.copy(
                            activitySelectorState = newActivitySelectorState,
                            startDateTime = start ?: state.startDateTime,
                            endDateTime = end ?: state.endDateTime
                        )
                    }
                }

                var editState by rememberSaveable { mutableStateOf(initialState) }
                NewActivity(
                    editState = editState,
                    onState = { editState = it },
                    initialState = initialState,
                    localizationRepository = viewModel.repository.localizationRepository,
                    onSave = {
                        scope.launch {
                            viewModel.repository.saveActivity(editState.toActivity(null))
                        }
                        context.finishOrGoBack(navController)
                    },
                    onNavigateBack = { context.finishOrGoBack(navController) },
                    onNavigateNewPlace = { navController.navigate(SettingsViews.PlaceDialog()) },
                )
            }

            composable<Views.TrackDetails>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) }
            ) { backStackEntry ->
                val route: Views.TrackDetails = backStackEntry.toRoute()
                onCurrentView(route)

                ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                    TrackDetailsView(
                        activityId = route.activityId,
                        onEdit = { navController.navigate(Views.NewActivity(route.activityId)) },
                        onBack = { navController.popBackStack() },
                        onNavigateMap = { navController.navigate(Views.Map(it)) },
                        onNavigateGraph = { id, projection ->
                            navController.navigate(Views.TrackGraph(id, projection))
                        }
                    )
                }
            }

            composable<Views.TrackGraph> { backStackEntry ->
                val route: Views.TrackGraph = backStackEntry.toRoute()
                onCurrentView(route)

                ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                    TrackGraphView(
                        activityId = route.activityId,
                        projection = route.projection,
                        onBack = { navController.popBackStack() },
                    )
                }
            }

            composable<Views.Map> { backStackEntry ->
                val route: Views.Map = backStackEntry.toRoute()
                onCurrentView(route)

                ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                    MapView(
                        activityId = route.activityId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable<Views.SummaryView>(
                enterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
                },
                exitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down)
                },
                deepLinks = listOf(navDeepLink<Views.SummaryView>(basePath = "https://fitnesscalendar.inky.com/home"))
            ) { backStackEntry ->
                val route: Views.SummaryView = backStackEntry.toRoute()
                onCurrentView(route)

                ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                    SummaryView(
                        filter = filterState,
                        onBack = { navController.popBackStack() },
                        onNavigateFilter = {
                            navController.navigate(Views.FilterActivity)
                        },
                        onEditFilter = {
                            filterState = it
                            viewModel.addToFilterHistory(filterState)
                        },
                        onNavigateActivity = {
                            navController.navigate(Views.ActivityLog(it))
                        }
                    )
                }
            }

            dialog<Views.RecordActivity> {
                onCurrentView(Views.RecordActivity)
                RecordActivity(
                    onStart = {
                        scope.launch { viewModel.recordingRepository.startRecording(it) }
                        navController.popBackStack()
                    },
                    localizationRepository = viewModel.repository.localizationRepository,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable<Views.Statistics> { backStackEntry ->
                val route: Views.Statistics = backStackEntry.toRoute()
                onCurrentView(route)

                ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                    StatisticsView(
                        initialPeriod = route.initialPeriod,
                        onOpenDrawer = openDrawer,
                        onViewActivity = {
                            navController.navigate(Views.ActivityLog(it.uid))
                        },
                    )
                }
            }
            settingsDestination(
                sharedContentScope = this@SharedTransitionLayout,
                onOpenDrawer = openDrawer,
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() },
                onOpen = { onCurrentView(Views.Settings) }
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Immutable
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

private fun Context.finishOrGoBack(navController: NavHostController) {
    val activity = this as? Activity
    if (activity != null) {
        activity.finishAndRemoveTask()
    } else {
        navController.popBackStack()
    }
}