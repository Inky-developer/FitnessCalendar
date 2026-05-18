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
import com.inky.fitnesscalendar.db.entities.RichPlace
import com.inky.fitnesscalendar.repository.DatabaseRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Composition local for easier access to common database values
 */
data class DatabaseValues(
    val activityTypes: List<ActivityType>,
    val activityTypeNames: Map<String, ActivityType>,
    val activityTypeRows: List<List<ActivityType>>,
    val places: List<RichPlace>,
    val activityFilterChips: List<ActivityFilterChip>
) {
    companion object {
        fun flow(db: DatabaseRepository) = combine(
            db.getActivityTypes(),
            db.getActivityTypeNames(),
            db.getActivityTypeRows(),
            db.getPlaces(),
            db.getFilterHistoryItems()
                .map { item -> item.mapNotNull { it.toActivityFilterChip() } }
        ) { activityTypes, typeNames, activityTypeRows, places, activityFilterChips ->
            DatabaseValues(
                activityTypes = activityTypes,
                activityTypeNames = typeNames,
                activityTypeRows = activityTypeRows,
                places = places,
                activityFilterChips = activityFilterChips
            )
        }
    }
}

val localDatabaseValues =
    compositionLocalOf<DatabaseValues> { error("Database values are not loaded yet") }

@Composable
fun ProvideDatabaseValues(db: DatabaseRepository, content: @Composable () -> Unit) {
    val databaseValues by remember { DatabaseValues.flow(db) }.collectAsState(initial = null)

    databaseValues?.let {
        CompositionLocalProvider(value = localDatabaseValues provides it) {
            content()
        }
    }
}