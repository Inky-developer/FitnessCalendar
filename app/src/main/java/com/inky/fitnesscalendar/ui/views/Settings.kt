package com.inky.fitnesscalendar.ui.views

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.inky.fitnesscalendar.ui.ProvideSharedContent
import com.inky.fitnesscalendar.ui.views.settings.ActivityTypeView
import com.inky.fitnesscalendar.ui.views.settings.EditPlaceDialog
import com.inky.fitnesscalendar.ui.views.settings.ImportExport
import com.inky.fitnesscalendar.ui.views.settings.PlaceListView
import com.inky.fitnesscalendar.ui.views.settings.SettingsDebug
import com.inky.fitnesscalendar.ui.views.settings.SettingsView
import com.inky.fitnesscalendar.ui.views.settings.SettingsViews


@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.settingsDestination(
    sharedContentScope: SharedTransitionScope,
    onNavigate: (Any) -> Unit,
    onOpenDrawer: () -> Unit,
    onBack: () -> Unit,
    onOpen: () -> Unit,
) {
    navigation<Views.Settings>(startDestination = SettingsViews.Main) {
        composable<SettingsViews.Main> {
            onOpen()
            ProvideSharedContent(sharedContentScope = sharedContentScope) {
                SettingsView(
                    onOpenDrawer,
                    onNavigateTypes = { onNavigate(SettingsViews.ActivityType) },
                    onNavigateDebug = { onNavigate(SettingsViews.Debug) },
                    onNavigatePlaces = { onNavigate(SettingsViews.PlaceList) },
                    onNavigateImportExport = { onNavigate(SettingsViews.ImportExport) }
                )
            }
        }

        composable<SettingsViews.Debug>(
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) }
        ) {
            SettingsDebug()
        }

        composable<SettingsViews.ActivityType>(
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) }
        ) {
            ActivityTypeView(onBack = onBack)
        }

        composable<SettingsViews.PlaceList> {
            PlaceListView(
                onOpenDrawer = onOpenDrawer,
                onEditPlace = { onNavigate(SettingsViews.PlaceDialog(it?.uid ?: -1)) })
        }

        dialog<SettingsViews.PlaceDialog> { backStackEntry ->
            val route: SettingsViews.PlaceDialog = backStackEntry.toRoute()
            EditPlaceDialog(
                initialPlaceId = route.placeId,
                onDismiss = onBack,
            )
        }

        composable<SettingsViews.ImportExport> {
            ImportExport(
                onOpenDrawer = onOpenDrawer
            )
        }
    }
}

