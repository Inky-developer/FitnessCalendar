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
    val activityTypes: List<ActivityType> = emptyList(),
    val activityTypeNames: Map<String, ActivityType> = emptyMap(),
    val activityTypeRows: List<List<ActivityType>> = emptyList(),
    val places: List<Place> = emptyList(),
    val activityFilterChips: List<ActivityFilterChip> = emptyList()
)

val localDatabaseValues = compositionLocalOf { DatabaseValues() }

@Composable
fun ProvideDatabaseValues(repository: DatabaseRepository, content: @Composable () -> Unit) {
    val activityTypes by repository
        .getActivityTypes()
        .collectAsState(initial = emptyList())
    val typeNames by repository.getActivityTypeNames().collectAsState(initial = emptyMap())
    val activityTypeRows by repository
        .getActivityTypeRows()
        .collectAsState(initial = emptyList())
    val places by repository
        .getPlaces()
        .collectAsState(initial = emptyList())
    val activityFilterChips by repository.getFilterHistoryItems()
        .map { item -> item.mapNotNull { it.toActivityFilterChip() } }
        .collectAsState(initial = emptyList())
    val databaseValues =
        remember(activityTypes, activityTypeRows, places, typeNames, activityFilterChips) {
            DatabaseValues(
                activityTypes = activityTypes,
                activityTypeNames = typeNames,
                activityTypeRows = activityTypeRows,
                places = places,
                activityFilterChips = activityFilterChips
            )
        }

    CompositionLocalProvider(value = localDatabaseValues provides databaseValues) {
        content()
    }
}