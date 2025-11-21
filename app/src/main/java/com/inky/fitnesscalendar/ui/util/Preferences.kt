package com.inky.fitnesscalendar.ui.util

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.inky.fitnesscalendar.preferences.Preference
import com.inky.fitnesscalendar.view_model.statistics.Projection
import kotlinx.coroutines.flow.combine
import java.util.Date

data class Preferences(
    val statisticsProjection: Projection,
    val backupUri: Uri?,
    val enablePublicApi: Boolean,
    val watchedFolders: Set<Uri>,
    val watchedFoldersLastImport: Date
) {
    companion object {
        fun flow(context: Context) = combine(
            Preference.PREF_BACKUP_URI.flow(context),
            Preference.PREF_ENABLE_PUBLIC_API.flow(context),
            Preference.PREF_STATS_PROJECTION.flow(context),
            Preference.PREF_WATCHED_FOLDERS.flow(context),
            Preference.PREF_WATCHED_FOLDERS_LAST_IMPORT.flow(context)
        )
        { backupUri, enablePublicApi, statisticsProjection, watchedFolders, watchedFoldersLastImport ->
            Preferences(
                statisticsProjection = statisticsProjection,
                backupUri = backupUri,
                enablePublicApi = enablePublicApi,
                watchedFolders = watchedFolders,
                watchedFoldersLastImport = watchedFoldersLastImport
            )
        }
    }
}

val localPreferences =
    compositionLocalOf<Preferences> { error("Preferences are not loaded yet") }

@Composable
fun ProvidePreferences(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val preferences by remember { Preferences.flow(context) }.collectAsState(initial = null)

    preferences?.let {
        CompositionLocalProvider(localPreferences provides it, content)
    }
}
