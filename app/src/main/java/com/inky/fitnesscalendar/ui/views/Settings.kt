package com.inky.fitnesscalendar.ui.views

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.inky.fitnesscalendar.ui.ProvideSharedContent
import com.inky.fitnesscalendar.ui.views.settings.ActivityTypeView
import com.inky.fitnesscalendar.ui.views.settings.SettingsDebug
import com.inky.fitnesscalendar.ui.views.settings.SettingsView
import com.inky.fitnesscalendar.ui.views.settings.SettingsViews


@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.settingsDestination(
    sharedContentScope: SharedTransitionScope,
    onNavigate: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    onBack: () -> Unit,
    onOpen: () -> Unit,
) {
    navigation(startDestination = SettingsViews.Main.navId, route = Views.Settings.getPath()) {
        composable(route = SettingsViews.Main.navId) {
            onOpen()
            ProvideSharedContent(sharedContentScope = sharedContentScope) {
                SettingsView(
                    onOpenDrawer,
                    onNavigateTypes = { onNavigate(SettingsViews.ActivityType.navId) },
                    onNavigateDebug = { onNavigate(SettingsViews.Debug.navId) }
                )
            }
        }

        composable(
            route = SettingsViews.Debug.navId,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) }
        ) {
            SettingsDebug()
        }

        composable(
            route = SettingsViews.ActivityType.navId,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) }
        ) {
            ActivityTypeView(onBack = onBack)
        }
    }
}

