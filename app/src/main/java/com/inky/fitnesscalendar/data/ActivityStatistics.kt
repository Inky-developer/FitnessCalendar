package com.inky.fitnesscalendar.data

import com.inky.fitnesscalendar.util.toDate
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ActivityStatistics(
    val activities: List<Activity>,
) {
    val size
        get() = activities.size

    fun isEmpty() = activities.isEmpty()

    fun isNotEmpty() = activities.isNotEmpty()

    val activitiesByCategory: Map<ActivityCategory, ActivityStatistics>
        get() = activities.groupBy { it.type.activityCategory }.toList()
            .sortedBy { (_, v) -> -v.size }
            .toMap()
            .mapValues { ActivityStatistics(it.value) }

    val activitiesByDay: Map<Int, ActivityStatistics>
        get() = keepNewerThanOneYear().groupByCalendarConstant(Calendar.DAY_OF_YEAR)

    val activitiesByWeek: Map<Int, ActivityStatistics>
        get() {
            val dayOfWeekField = WeekFields.of(Locale.getDefault()).dayOfWeek()
            val firstDateToInclude =
                LocalDate.now().minusYears(1).plusWeeks(1).with(dayOfWeekField, 1).atStartOfDay()
            return keepNewer(firstDateToInclude.toDate()).groupByCalendarConstant(Calendar.WEEK_OF_YEAR)
        }

    val activitiesByMonth: Map<Int, ActivityStatistics>
        get() {
            val today = LocalDate.now()
            val firstDateToInclude =
                today.minusYears(1).withDayOfMonth(1).plusMonths(1).atStartOfDay()
            return keepNewer(firstDateToInclude.toDate())
                .groupByCalendarConstant(Calendar.MONTH)
                .mapKeys { it.key + 1 }
        }

    val activitiesByYear: Map<Int, ActivityStatistics>
        get() = groupByCalendarConstant(Calendar.YEAR)

    private fun groupByCalendarConstant(calendarConstant: Int): Map<Int, ActivityStatistics> {
        val calendar = Calendar.getInstance()
        return activities
            .groupBy { calendar.apply { time = it.startTime }.get(calendarConstant) }
            .mapValues { ActivityStatistics(it.value) }
    }

    fun filter(func: (Activity) -> Boolean) = ActivityStatistics(activities.filter(func))

    private fun keepNewer(date: Date): ActivityStatistics = filter { it.startTime.after(date) }

    private fun keepNewerThanOneYear() =
        keepNewer(LocalDate.now().minusYears(1).plusDays(1).atStartOfDay().toDate())
}