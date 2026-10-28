package com.inky.fitnesscalendar.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.inky.fitnesscalendar.preferences.dataStore

data class Preferences(
    val store: androidx.datastore.preferences.core.Preferences
)

val localPreferences =
    compositionLocalOf<Preferences> { error("Preferences are not loaded yet") }

@Composable
fun ProvidePreferences(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val store by remember { context.dataStore.data }.collectAsState(initial = null)

    store?.let {
        CompositionLocalProvider(localPreferences provides Preferences(it), content)
    }
}
