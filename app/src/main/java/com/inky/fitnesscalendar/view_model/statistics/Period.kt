package com.inky.fitnesscalendar.view_model.statistics

import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityStatistics
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.Locale

/**
 * A time period for the statistics view.
 *
 * This decides by what time span values should be displayed (E.g. show individual days or group activities by week)
 */
enum class Period(
    val nameId: Int,
    val xLabelId: Int,
    val numVisibleDays: Double,
    private val temporalUnit: TemporalUnit
) {
    Day(R.string.days, R.string.day, 7.0, ChronoUnit.DAYS),
    Week(R.string.weeks, R.string.week, 5.0, ChronoUnit.WEEKS),
    Month(R.string.months, R.string.month, 6.0, ChronoUnit.MONTHS),
    Year(R.string.years, R.string.year, 4.0, ChronoUnit.YEARS);

    /**
     * Groups the given statistics to a list of value and x-axis label for that group
     */
    fun filter(statistics: ActivityStatistics): Map<Long, StatisticsEntry> {
        // TODO: Filter out older statistics already in the db query
        val today = LocalDate.now()

        val result = mutableMapOf<Long, StatisticsEntry>()
        val activityMap = groupStats(statistics)
        // Subtract 1 unit to make sure there are at least two data points
        // Otherwise, the graph might not render correctly
        var day = (activityMap.keys.minOrNull() ?: today).minus(1, temporalUnit)
        var index = 0L
        while (!day.isAfter(today)) {
            result[index] = StatisticsEntry(
                statistics = activityMap[day] ?: ActivityStatistics(emptyList()),
                entryName = format(day, today)
            )
            day = day.plus(1, temporalUnit)
            index += 1
        }

        return result
    }

    private fun groupStats(statistics: ActivityStatistics) = when (this) {
        Day -> statistics.activitiesByDay
        Week -> statistics.activitiesByWeek
        Month -> statistics.activitiesByMonth
        Year -> statistics.activitiesByYear
    }

    private fun format(date: LocalDate, today: LocalDate) = when (this) {
        Day, Week -> formatRelativeDay(date, today)
        Month -> date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        Year -> date.year.toString()
    }

    private fun formatRelativeDay(day: LocalDate, today: LocalDate): String {
        return if (today.minusWeeks(1).isBefore(day)) {
            day.dayOfWeek.getDisplayName(
                TextStyle.SHORT,
                Locale.getDefault()
            )
        } else {
            day.format(dateFormatter)
        }
    }

    data class StatisticsEntry(val statistics: ActivityStatistics, val entryName: String)

    companion object {
        private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
    }
}