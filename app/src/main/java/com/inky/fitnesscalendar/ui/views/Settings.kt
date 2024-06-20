package com.inky.fitnesscalendar.ui.views

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.inky.fitnesscalendar.ui.ProvideSharedContent
import com.inky.fitnesscalendar.ui.views.settings.SettingsDebug
import com.inky.fitnesscalendar.ui.views.settings.SettingsView
import com.inky.fitnesscalendar.ui.views.settings.SettingsViews


@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.settingsDestination(
    sharedContentScope: SharedTransitionScope,
    onNavigate: (String) -> Unit,
    onOpenDrawer: () -> Unit
) {
    navigation(startDestination = SettingsViews.Main.navId, route = Views.Settings.getPath()) {
        composable(route = SettingsViews.Main.navId) {
            ProvideSharedContent(sharedContentScope = sharedContentScope) {
                SettingsView(
                    onOpenDrawer,
                    onNavigateDebug = { onNavigate(SettingsViews.Debug.navId) })
            }
        }

        composable(route = SettingsViews.Debug.navId) {
            SettingsDebug()
        }
    }
}

