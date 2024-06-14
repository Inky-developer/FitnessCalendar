package com.inky.fitnesscalendar.view_model.statistics

import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityStatistics
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * A time period for the statistics view.
 *
 * This decides by what time span values should be displayed (E.g. show individual days or group activities by week)
 */
enum class Period(val nameId: Int, val xLabelId: Int, val numVisibleEntries: Float) {
    Day(R.string.days, R.string.day, 7f),
    Week(R.string.weeks, R.string.week, 5f),
    Month(R.string.months, R.string.month, 12f),
    Year(R.string.years, R.string.year, 4f);

    /**
     * Groups the given statistics to a list of value and x-axis label for that group
     */
    fun filter(statistics: ActivityStatistics): List<Pair<ActivityStatistics, String>> {
        val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        val woyField = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()
        val dayOfWeekField = WeekFields.of(Locale.getDefault()).dayOfWeek()

        val result: MutableList<Pair<ActivityStatistics?, String>> = mutableListOf()

        val today = LocalDate.now().atStartOfDay()

        val relativeDayFormat: (LocalDateTime) -> String = { day ->
            if (!day.isBefore(today.minusWeeks(1))) {
                day.dayOfWeek.getDisplayName(
                    TextStyle.SHORT,
                    Locale.getDefault()
                )
            } else {
                day.format(dateFormatter)
            }
        }

        // TODO: Filter out older statistics already in the db query
        when (this) {
            Day -> {
                var day = today.minusYears(1).with(dayOfWeekField, 1).plusWeeks(1)
                val activityMap = statistics.activitiesByDay
                while (!day.isAfter(today)) {
                    result.add(
                        (activityMap[day.dayOfYear]) to relativeDayFormat(day)
                    )
                    day = day.plusDays(1)
                }
            }

            Week -> {
                var day = today.minusYears(1).with(dayOfWeekField, 1).plusWeeks(1)
                val activityMap = statistics.activitiesByWeek
                while (!day.isAfter(today)) {
                    result.add(
                        (activityMap[day.get(woyField)]) to day.format(dateFormatter)
                    )
                    day = day.plusWeeks(1)
                }
            }

            Month -> {
                var day = today.minusYears(1).withDayOfMonth(1).plusMonths(1)
                val activityMap = statistics.activitiesByMonth
                while (!day.isAfter(today)) {
                    result.add(
                        (activityMap[day.monthValue]) to day.month.getDisplayName(
                            TextStyle.SHORT,
                            Locale.getDefault()
                        )
                    )
                    day = day.plusMonths(1)
                }
            }

            Year -> {
                val activityMap = statistics.activitiesByYear
                val firstYear = activityMap.keys.min()
                val currentYear = today.year
                for (year in firstYear..currentYear) {
                    result.add((activityMap[year]) to year.toString())
                }
            }
        }

        return result.map { (it.first ?: ActivityStatistics(emptyList())) to it.second }
    }
}