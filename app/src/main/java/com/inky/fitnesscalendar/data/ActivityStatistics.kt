package com.inky.fitnesscalendar.data

import com.inky.fitnesscalendar.data.measure.Distance
import com.inky.fitnesscalendar.data.measure.Duration
import com.inky.fitnesscalendar.data.measure.Duration.Companion.until
import com.inky.fitnesscalendar.data.measure.Velocity
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.TypeActivity
import com.inky.fitnesscalendar.util.toDate
import com.inky.fitnesscalendar.util.toLocalDate
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

data class ActivityStatistics(
    val activities: List<TypeActivity>,
) {
    val size
        get() = activities.size

    fun totalTime() =
        Duration(activities.sumOf { it.activity.startTime.until(it.activity.endTime).elapsedMs })

    fun averageTime() = Duration(totalTime().elapsedMs / size)

    fun totalDistance() = Distance(meters = activities.sumOf { it.activity.distance?.meters ?: 0 })

    fun averageDistance(): Distance {
        val distances = activities.mapNotNull { it.activity.distance?.meters }
        if (distances.isEmpty()) {
            return Distance(meters = 0)
        }
        return Distance(meters = (distances.sum().toDouble() / distances.size).roundToLong())
    }

    fun averageVelocity(): Velocity {
        val elapsedSeconds = averageTime().elapsedSeconds
        if (elapsedSeconds == 0.0) {
            return Velocity(metersPerSecond = 0.0)
        }
        return Velocity(metersPerSecond = averageDistance().meters / elapsedSeconds)
    }

    fun averageIntensity(): Double {
        val intensities = activities.mapNotNull { it.activity.intensity?.value }
        if (intensities.isEmpty()) {
            return 0.0
        }
        return intensities.sumOf { it.toDouble() } / intensities.size
    }

    fun isEmpty() = activities.isEmpty()

    val activitiesByCategory: Map<ActivityCategory, ActivityStatistics>
        get() = activities
            .groupBy { it.type.activityCategory }
            .toList()
            .sortedBy { (_, v) -> -v.size }
            .toMap()
            .mapValues { ActivityStatistics(it.value) }

    val activitiesByType: Map<ActivityType, ActivityStatistics>
        get() = activities
            .groupBy { it.type }
            .toList()
            .sortedBy { (_, v) -> -v.size }
            .toMap()
            .mapValues { ActivityStatistics(it.value) }


    val activitiesByDay: Map<LocalDate, ActivityStatistics>
        get() = keepNewerThanOneYear().groupByLocalDate { it }

    val activitiesByWeek: Map<LocalDate, ActivityStatistics>
        get() {
            val dayOfWeekField = WeekFields.of(Locale.getDefault()).dayOfWeek()
            val firstDateToInclude =
                LocalDate.now().minusYears(1).plusWeeks(1).with(dayOfWeekField, 1).atStartOfDay()
            return keepNewer(firstDateToInclude.toDate()).groupByLocalDate {
                it.with(dayOfWeekField, 1)
            }
        }

    val activitiesByMonth: Map<LocalDate, ActivityStatistics>
        get() {
            val today = LocalDate.now()
            val firstDateToInclude =
                today.minusYears(1).withDayOfMonth(1).plusMonths(1).atStartOfDay()
            return keepNewer(firstDateToInclude.toDate())
                .groupByLocalDate { it.withDayOfMonth(1) }
        }

    val activitiesByYear: Map<LocalDate, ActivityStatistics>
        get() = groupByLocalDate { it.withDayOfYear(1) }

    private fun groupByLocalDate(func: (LocalDate) -> LocalDate): Map<LocalDate, ActivityStatistics> {
        val zoneId = ZoneId.systemDefault()
        return groupBy { func(it.activity.startTime.toLocalDate(zoneId)) }
    }

    private fun <T> groupBy(func: (TypeActivity) -> T): Map<T, ActivityStatistics> =
        activities.groupBy(func).mapValues { ActivityStatistics(it.value) }

    fun filter(func: (TypeActivity) -> Boolean) = ActivityStatistics(activities.filter(func))

    private fun keepNewer(date: Date): ActivityStatistics =
        filter { it.activity.startTime.after(date) }

    private fun keepNewerThanOneYear() =
        keepNewer(LocalDate.now().minusYears(1).plusDays(1).atStartOfDay().toDate())
}