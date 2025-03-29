package com.inky.fitnesscalendar.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilterChip
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilterChip.Companion.toActivityFilterChip
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.repository.DatabaseRepository
import kotlinx.coroutines.flow.map

/**
 * Composition local for easier access to common database values
 */
data class DatabaseValues(
    val activityTypes: List<ActivityType>,
    val activityTypeNames: Map<String, ActivityType>,
    val activityTypeRows: List<List<ActivityType>>,
    val places: List<Place>,
    val activityFilterChips: List<ActivityFilterChip>
)

val localDatabaseValues =
    compositionLocalOf<DatabaseValues> { error("Database values are not loaded yet") }

@Composable
fun ProvideDatabaseValues(repository: DatabaseRepository, content: @Composable () -> Unit) {
    val activityTypes by repository
        .getActivityTypes()
        .collectAsState(initial = null)
    val typeNames by repository.getActivityTypeNames().collectAsState(initial = null)
    val activityTypeRows by repository
        .getActivityTypeRows()
        .collectAsState(initial = null)
    val places by repository
        .getPlaces()
        .collectAsState(initial = null)
    val activityFilterChips by repository.getFilterHistoryItems()
        .map { item -> item.mapNotNull { it.toActivityFilterChip() } }
        .collectAsState(initial = null)
    val databaseValues =
        remember(activityTypes, activityTypeRows, places, typeNames, activityFilterChips) {
            DatabaseValues(
                activityTypes = activityTypes ?: return@remember null,
                activityTypeNames = typeNames ?: return@remember null,
                activityTypeRows = activityTypeRows ?: return@remember null,
                places = places ?: return@remember null,
                activityFilterChips = activityFilterChips ?: return@remember null
            )
        }

    if (databaseValues != null) {
        CompositionLocalProvider(value = localDatabaseValues provides databaseValues) {
            content()
        }
    }
}