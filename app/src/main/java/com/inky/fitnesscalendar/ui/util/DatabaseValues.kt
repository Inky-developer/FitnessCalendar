package com.inky.fitnesscalendar.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.db.entities.ActivityType

/**
 * Composition local for easier access to common database values
 */
data class DatabaseValues(
    val activityTypes: List<ActivityType> = emptyList(),
    val activityTypeRows: List<List<ActivityType>> = emptyList(),
)

val localDatabaseValues = compositionLocalOf { DatabaseValues() }

@Composable
fun ProvideDatabaseValues(repository: AppRepository, content: @Composable () -> Unit) {
    val activityTypes by repository
        .getActivityTypes()
        .collectAsState(initial = emptyList())
    val activityTypeRows by repository
        .getActivityTypeRows()
        .collectAsState(initial = emptyList())
    val databaseValues = remember(activityTypes, activityTypeRows) {
        DatabaseValues(
            activityTypes = activityTypes,
            activityTypeRows = activityTypeRows
        )
    }

    CompositionLocalProvider(value = localDatabaseValues provides databaseValues) {
        content()
    }
}