package com.inky.fitnesscalendar.ui.views

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.inky.fitnesscalendar.ui.ProvideSharedContent
import com.inky.fitnesscalendar.ui.views.settings.ActivityTypeView
import com.inky.fitnesscalendar.ui.views.settings.BackupView
import com.inky.fitnesscalendar.ui.views.settings.EditPlaceDialog
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
                    onNavigateBackup = { onNavigate(SettingsViews.Backup) }
                )
            }
        }

        settingsComposable<SettingsViews.Debug> {
            SettingsDebug()
        }

        settingsComposable<SettingsViews.ActivityType> {
            ActivityTypeView(onBack = onBack)
        }

        settingsComposable<SettingsViews.PlaceList> {
            PlaceListView(
                onBack = onBack,
                onEditPlace = { onNavigate(SettingsViews.PlaceDialog(it?.uid)) })
        }

        dialog<SettingsViews.PlaceDialog> { backStackEntry ->
            val route: SettingsViews.PlaceDialog = backStackEntry.toRoute()
            EditPlaceDialog(
                initialPlaceId = route.placeId,
                onDismiss = onBack,
            )
        }

        settingsComposable<SettingsViews.Backup> {
            BackupView(onBack = onBack)
        }
    }
}

private inline fun <reified T : Any> NavGraphBuilder.settingsComposable(noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit) =
    composable<T>(
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) },
        content = content
    )