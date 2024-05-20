package com.inky.fitnesscalendar.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.inky.fitnesscalendar.data.ActivityFilter
import com.inky.fitnesscalendar.ui.views.FilterActivity
import com.inky.fitnesscalendar.ui.views.NewActivity
import com.inky.fitnesscalendar.ui.views.Today
import com.inky.fitnesscalendar.ui.views.View
import com.inky.fitnesscalendar.view_model.AppViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun App(viewModel: AppViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    var isNewActivityDialogOpen by rememberSaveable { mutableStateOf(false) }
    // TODO: Make saveable
    var filterState by remember { mutableStateOf(ActivityFilter()) }

    SharedTransitionLayout {
        NavHost(navController = navController, startDestination = View.TODAY.getPath()) {
            composable(View.TODAY.pathTemplate()) {
                Today(
                    filter = filterState,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable,
                    onNewActivity = {
                        navController.navigate(View.NEW_ACTIVITY.getPath(-1))
                    },
                    onEditActivity = {
                        navController.navigate(View.NEW_ACTIVITY.getPath(it.uid ?: -1))
                    },
                    onFilter = {
                        navController.navigate(View.FILTER_ACTIVITY.getPath())
                    },
                    isNewActivityOpen = isNewActivityDialogOpen
                )
            }
            composable(View.FILTER_ACTIVITY.pathTemplate(), enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
            }, exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down)
            }) {
                FilterActivity(filterState,
                    onFilterChange = { filterState = it },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable,
                    onBack = { navController.popBackStack() })
            }
            dialog(
                View.NEW_ACTIVITY.pathTemplate(), arguments = View.NEW_ACTIVITY.navArgs()
            ) { backStackEntry ->
                isNewActivityDialogOpen = true
                val activityId =
                    backStackEntry.arguments?.let { View.Argument.ACTIVITY_ID.extract(it) }
                NewActivity(activityId, onSave = {
                    scope.launch {
                        viewModel.repository.saveActivity(it)
                    }
                    isNewActivityDialogOpen = false
                    navController.popBackStack()
                }, onNavigateBack = {
                    isNewActivityDialogOpen = false
                    navController.popBackStack()
                })
            }
            composable(View.SETTINGS.pathTemplate()) {
                Text("Settings")
            }
        }
    }
}
