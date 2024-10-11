package com.inky.fitnesscalendar.view_model.statistics

import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.Displayable
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.db.entities.ActivityType

/**
 * A grouping of activities.
 *
 * Specifies how the activities should be grouped in the statistics view.
 *
 * Three options:
 *  - All: activities will be grouped by their category
 *  - Category: activities will be grouped by their type and filtered to be in the category
 *  - Type: activities will be grouped by their type and filtered by their type
 */
interface Grouping {
    fun filter(): ActivityFilter

    fun apply(statistics: ActivityStatistics): Map<out Displayable, ActivityStatistics>

    fun options(): List<Displayable>

    data object All : Grouping {
        override fun filter() = ActivityFilter()

        override fun apply(statistics: ActivityStatistics) = statistics.activitiesByCategory

        override fun options() = ActivityCategory.entries
    }

    data class Category(val category: ActivityCategory, val activityTypes: List<ActivityType>) :
        Grouping {
        override fun filter() = ActivityFilter(categories = listOf(category))

        override fun apply(statistics: ActivityStatistics) =
            statistics.activitiesByCategory.filter { it.key == category } + statistics.activitiesByType

        override fun options() =
            listOf(category) + activityTypes.filter { it.activityCategory == category }
    }

    data class Type(val type: ActivityType) : Grouping {
        override fun filter() = ActivityFilter(types = listOf(type))

        override fun apply(statistics: ActivityStatistics) = statistics.activitiesByType

        override fun options() = listOf(type)
    }
}