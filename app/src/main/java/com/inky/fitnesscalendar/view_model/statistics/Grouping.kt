package com.inky.fitnesscalendar.view_model.statistics

import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.Displayable
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
    fun filterCategory(): ActivityCategory?

    fun apply(statistics: ActivityStatistics): List<Group>

    data class Group(
        val value: Displayable,
        val stats: ActivityStatistics,
        val enabled: Boolean = true
    )

    data object All : Grouping {
        override fun filterCategory() = null

        override fun apply(statistics: ActivityStatistics) =
            statistics.activitiesByCategory
                .map { (k, v) -> Group(k, v) }
                .sortedBy { -it.stats.size }
    }

    data class Category(val category: ActivityCategory, val activityTypes: List<ActivityType>) :
        Grouping {
        override fun filterCategory() = category

        override fun apply(statistics: ActivityStatistics) =
            (statistics.activitiesByCategory.filter { it.key == category }
                .map { (k, v) -> Group(k, v) }
                    + statistics.activitiesByType.filter { (k, _) -> k.activityCategory == category }
                .map { (k, v) ->
                    Group(k, v, enabled = !k.archived || v.isEmpty())
                }).sortedBy { -it.stats.size }

    }
}