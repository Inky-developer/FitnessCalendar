package com.inky.fitnesscalendar.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
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
import com.inky.fitnesscalendar.ui.views.FilterActivity
import com.inky.fitnesscalendar.ui.views.Home
import com.inky.fitnesscalendar.ui.views.ImportExport
import com.inky.fitnesscalendar.ui.views.NewActivity
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
        }) {
        SharedTransitionLayout {
            NavHost(navController = navController, startDestination = View.Home.getPath()) {
                composable(View.Home.pathTemplate()) {
                    currentView = View.Home
                    Home(
                        isNewActivityOpen = isNewActivityOpen,
                        onNewActivity = { navController.navigate(View.NewActivity.getPath(-1)) },
                        onNavigateActivity = {
                            navController.navigate(View.ActivityLog.getPath())
                        },
                        onOpenDrawer = openDrawer
                    )
                }
                composable(View.ActivityLog.pathTemplate()) {
                    currentView = View.ActivityLog
                    ActivityLog(
                        filter = filterState,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@composable,
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
                        isNewActivityOpen = isNewActivityOpen
                    )
                }
                composable(View.FilterActivity.pathTemplate(), enterTransition = {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
                }, exitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down)
                }) {
                    currentView = View.FilterActivity
                    FilterActivity(filterState,
                        onFilterChange = { filterState = it },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@composable,
                        onBack = { navController.popBackStack() })
                }
                dialog(
                    View.NewActivity.pathTemplate(), arguments = View.NewActivity.navArgs()
                ) { backStackEntry ->
                    currentView = View.NewActivity
                    isNewActivityOpen = true
                    val activityId =
                        backStackEntry.arguments?.let { View.Argument.ACTIVITY_ID.extract(it) }
                    NewActivity(activityId, onSave = {
                        scope.launch {
                            viewModel.repository.saveActivity(it)
                        }
                        isNewActivityOpen = false
                        navController.popBackStack()
                    }, onNavigateBack = {
                        isNewActivityOpen = false
                        navController.popBackStack()
                    })
                }
                composable(View.ImportExport.pathTemplate()) {
                    currentView = View.ImportExport
                    ImportExport(
                        onOpenDrawer = openDrawer
                    )
                }
                composable(View.Settings.pathTemplate()) {
                    currentView = View.Settings
                    Text("Settings")
                }
            }
        }
    }
}
