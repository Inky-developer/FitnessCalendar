package com.inky.fitnesscalendar.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.Place

/**
 * Composition local for easier access to common database values
 */
data class DatabaseValues(
    val activityTypes: List<ActivityType> = emptyList(),
    val activityTypeRows: List<List<ActivityType>> = emptyList(),
    val places: List<Place> = emptyList()
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
    val places by repository
        .getPlaces()
        .collectAsState(initial = emptyList())
    val databaseValues = remember(activityTypes, activityTypeRows, places) {
        DatabaseValues(
            activityTypes = activityTypes,
            activityTypeRows = activityTypeRows,
            places = places
        )
    }

    CompositionLocalProvider(value = localDatabaseValues provides databaseValues) {
        content()
    }
}