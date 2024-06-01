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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.inky.fitnesscalendar.data.ActivityFilter
import com.inky.fitnesscalendar.ui.components.NavigationDrawer
import com.inky.fitnesscalendar.ui.views.ActivityLog
import com.inky.fitnesscalendar.ui.views.FilterView
import com.inky.fitnesscalendar.ui.views.Home
import com.inky.fitnesscalendar.ui.views.ImportExport
import com.inky.fitnesscalendar.ui.views.NewActivity
import com.inky.fitnesscalendar.ui.views.RecordActivity
import com.inky.fitnesscalendar.ui.views.Settings
import com.inky.fitnesscalendar.ui.views.View
import com.inky.fitnesscalendar.view_model.AppViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun App(viewModel: AppViewModel = hiltViewModel()) {
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
    var currentView by rememberSaveable { mutableStateOf<View?>(null) }
    var isNewActivityOpen by rememberSaveable { mutableStateOf(false) }

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
                startDestination = View.Home.getPath(),
            ) {

                composable(
                    View.Home.pathTemplate(),
                    enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End) },
                    exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start) }
                ) {
                    currentView = View.Home
                    ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                        Home(
                            isNewActivityOpen = isNewActivityOpen,
                            onNewActivity = { navController.navigate(View.NewActivity.getPath(-1)) },
                            onEditActivity = {
                                navController.navigate(View.NewActivity.getPath(it.uid ?: -1))
                            },
                            onRecordActivity = { navController.navigate(View.RecordActivity.getPath()) },
                            onNavigateActivity = {
                                navController.navigate(View.ActivityLog.getPath())
                            },
                            onOpenDrawer = openDrawer
                        )
                    }
                }
                composable(View.ActivityLog.pathTemplate()) {
                    currentView = View.ActivityLog
                    ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                        ActivityLog(
                            filter = filterState,
                            onOpenDrawer = openDrawer,
                            onNewActivity = {
                                navController.navigate(View.NewActivity.getPath(-1))
                            },
                            onEditActivity = {
                                navController.navigate(View.NewActivity.getPath(it.uid ?: -1))
                            },
                            onFilter = {
                                navController.navigate(View.FilterActivity.getPath())
                            },
                            onEditFilter = {
                                filterState = it
                            },
                            isNewActivityOpen = isNewActivityOpen
                        )
                    }
                }
                composable(View.FilterActivity.pathTemplate(), enterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
                }, exitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down)
                }) {
                    currentView = View.FilterActivity
                    ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                        FilterView(
                            filterState,
                            onFilterChange = { filterState = it },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                dialog(
                    View.NewActivity.pathTemplate(), arguments = View.NewActivity.navArgs()
                ) { backStackEntry ->
                    currentView = View.NewActivity
                    isNewActivityOpen = true
                    val activityId =
                        backStackEntry.arguments?.let { View.Argument.ACTIVITY_ID.extract(it) }
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
                    View.RecordActivity.pathTemplate(), arguments = View.RecordActivity.navArgs()
                ) {
                    currentView = View.RecordActivity
                    RecordActivity(
                        onStart = {
                            scope.launch {
                                viewModel.repository.startRecording(it)
                            }
                            navController.popBackStack()
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(View.ImportExport.pathTemplate()) {
                    currentView = View.ImportExport
                    ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                        ImportExport(
                            onOpenDrawer = openDrawer
                        )
                    }
                }
                composable(View.Settings.pathTemplate()) {
                    currentView = View.Settings
                    ProvideSharedContent(sharedContentScope = this@SharedTransitionLayout) {
                        Settings()
                    }
                }
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
val localSharedTransition = compositionLocalOf { SharedTransitionData(null, null) }

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