package com.inky.fitnesscalendar.view_model.statistics

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarkerValueFormatter

/**
 * A Projection of [ActivityStatistics] to a value that can be displayed in the statistics view
 */
enum class Projection(
    @StringRes val legendTextId: Int,
    @StringRes val labelTextId: Int,
    @DrawableRes val iconId: Int,
    val verticalStepSize: Double?
) {
    ByTotalActivities(
        R.string.number_of_activities,
        R.string.by_total_activities,
        R.drawable.outline_numbers_24,
        2.0
    ),
    ByTotalTime(
        R.string.total_hours,
        R.string.by_total_time,
        R.drawable.outline_total_time_24,
        null
    ),
    ByAverageTime(
        R.string.average_hours,
        R.string.by_average_time,
        R.drawable.outline_timer_24,
        null
    ),
    ByTotalDistance(
        R.string.total_kilometers,
        R.string.total_distance,
        R.drawable.outline_total_distance_24,
        null,
    ),
    ByAverageDistance(
        R.string.average_kilometers,
        R.string.average_distance,
        R.drawable.outline_avg_distance_24,
        null
    ),
    ByAverageSpeed(
        R.string.average_kmh,
        R.string.average_speed,
        R.drawable.outline_speed_24,
        null
    ),
    ByAverageIntensity(
        R.string.average_intensity,
        R.string.average_intensity,
        R.drawable.twotone_lightbulb_24,
        1.0
    );

    fun apply(statistics: ActivityStatistics): Double? = when (this) {
        ByTotalTime -> statistics.totalTime().elapsedHours
        ByAverageTime -> statistics.averageTime()?.elapsedHours
        ByTotalActivities -> statistics.size.toDouble()
        ByTotalDistance -> statistics.totalDistance().kilometers
        ByAverageDistance -> statistics.averageDistance()?.kilometers
        ByAverageSpeed -> statistics.averageSpeed()?.kmh
        ByAverageIntensity -> statistics.averageIntensity()
    }

    fun markerFormatter() = when (this) {
        ByTotalTime, ByAverageTime -> TimeMarkerFormatter()
        ByTotalActivities, ByTotalDistance, ByAverageDistance, ByAverageSpeed, ByAverageIntensity -> DefaultCartesianMarkerValueFormatter()
    }

    /**
     * For non-cumulative projections, it makes more sense to interpolate missing values (return null)
     * This is not wanted for total projections though, since here it is important to know when it is zero (explicitly return 0).
     */
    fun getDefault(): Double? = when (this) {
        ByTotalTime, ByTotalActivities, ByTotalDistance, ByAverageTime, ByAverageDistance -> 0.0
        ByAverageSpeed, ByAverageIntensity -> null
    }
}