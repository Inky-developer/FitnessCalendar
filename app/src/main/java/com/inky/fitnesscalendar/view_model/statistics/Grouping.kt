package com.inky.fitnesscalendar.view_model.statistics

import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.ActivityType
import com.inky.fitnesscalendar.data.Displayable

/**
 * A grouping of activities.
 *
 * Specifies how the activities should be grouped in the statistics view.
 *
 * Two options:
 *  - When [category] is null, activities will be grouped by their category
 *  - When [category] is not null, activities will be grouped by their type and filtered to be in the category
 */
data class Grouping(val category: ActivityCategory?) {
    fun apply(statistics: ActivityStatistics): Map<out Any, ActivityStatistics> = when (category) {
        null -> statistics.activitiesByCategory
        else -> statistics.activitiesByType
    }

    fun options(): List<Displayable> = when (category) {
        null -> ActivityCategory.entries
        else -> ActivityType.entries.filter { it.activityCategory == category }
    }
}