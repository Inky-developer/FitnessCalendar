package com.inky.fitnesscalendar.ui.util

import androidx.compose.runtime.compositionLocalOf
import com.inky.fitnesscalendar.db.entities.ActivityType

/**
 * Composition local for easier access to common database values
 */
data class DatabaseValues(
    val activityTypes: List<ActivityType> = emptyList(),
    val activityTypeRows: List<List<ActivityType>> = emptyList(),
)

val localDatabaseValues = compositionLocalOf { DatabaseValues() }