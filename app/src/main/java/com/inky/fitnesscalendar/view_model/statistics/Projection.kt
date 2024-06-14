package com.inky.fitnesscalendar.view_model.statistics

import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityStatistics

/**
 * A Projection of [ActivityStatistics] to a value that can be displayed in the statistics view
 */
enum class Projection(val legendTextId: Int) {
    ByTotalTime(R.string.total_hours),
    ByAverageTime(R.string.average_hours),
    ByTotalActivities(R.string.number_of_activities);

    fun apply(statistics: ActivityStatistics): Double = when (this) {
        ByTotalTime -> statistics.totalTime().elapsedHours
        ByAverageTime -> statistics.averageTime().elapsedHours
        ByTotalActivities -> statistics.size.toDouble()
    }

    fun verticalStepSize() = when (this) {
        ByTotalTime, ByAverageTime -> null
        ByTotalActivities -> 2f
    }
}