package com.inky.fitnesscalendar.data

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date

data class ActivityStatistics(
    val activities: List<Activity>,
) {
    val size
        get() = activities.size

    val activitiesByCategory: Map<ActivityCategory, List<Activity>>
        get() = activities.groupBy { it.type.activityCategory }.toList()
            .sortedBy { (_, v) -> -v.size }
            .toMap()

    val activitiesByDay: Map<Int, ActivityStatistics>
        get() = keepNewerThanOneYear().groupByCalendarConstant(Calendar.DAY_OF_YEAR)

    val activitiesByWeek: Map<Int, ActivityStatistics>
        get() = keepNewerThanOneYear().groupByCalendarConstant(Calendar.WEEK_OF_YEAR)

    val activitiesByMonth: Map<Int, ActivityStatistics>
        get() = keepNewerThanOneYear().groupByCalendarConstant(Calendar.MONTH)

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
        keepNewer(Date.from(Instant.now() - ChronoUnit.YEARS.duration))
}