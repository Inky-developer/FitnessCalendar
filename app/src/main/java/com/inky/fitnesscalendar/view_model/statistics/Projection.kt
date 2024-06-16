package com.inky.fitnesscalendar.view_model.statistics

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityStatistics

/**
 * A Projection of [ActivityStatistics] to a value that can be displayed in the statistics view
 */
enum class Projection(
    @StringRes val legendTextId: Int,
    @StringRes val labelTextId: Int,
    @DrawableRes val iconId: Int,
) {
    ByTotalActivities(
        R.string.number_of_activities,
        R.string.by_total_activities,
        R.drawable.outline_numbers_24
    ),
    ByTotalTime(R.string.total_hours, R.string.by_total_time, R.drawable.outline_total_time_24),
    ByAverageTime(R.string.average_hours, R.string.by_average_time, R.drawable.outline_timer_24);

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