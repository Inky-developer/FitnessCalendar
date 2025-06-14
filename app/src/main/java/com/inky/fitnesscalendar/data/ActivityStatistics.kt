package com.inky.fitnesscalendar.data

import com.inky.fitnesscalendar.data.measure.Distance
import com.inky.fitnesscalendar.data.measure.Duration
import com.inky.fitnesscalendar.data.measure.HeartFrequency
import com.inky.fitnesscalendar.data.measure.Speed
import com.inky.fitnesscalendar.data.measure.Temperature
import com.inky.fitnesscalendar.data.measure.VerticalDistance
import com.inky.fitnesscalendar.data.measure.bpm
import com.inky.fitnesscalendar.data.measure.celsius
import com.inky.fitnesscalendar.data.measure.meters
import com.inky.fitnesscalendar.data.measure.metersPerSecond
import com.inky.fitnesscalendar.data.measure.ms
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.util.filteredMaxByOrNull
import com.inky.fitnesscalendar.util.toDate
import com.inky.fitnesscalendar.util.toLocalDate
import com.inky.fitnesscalendar.util.toLocalDateTime
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Date
import java.util.Locale

@JvmInline
value class ActivityStatistics(
    val activities: List<RichActivity>,
) {
    val size
        get() = activities.size

    fun totalTime() = activities.sumOf { it.activity.duration.elapsedMs }.ms()

    fun averageTime(): Duration? {
        val totalMs = totalTime().elapsedMs
        if (totalMs == 0L) return null
        return (totalTime().elapsedMs / size).ms()
    }

    fun totalDistance() = activities.sumOf { it.activity.distance?.meters ?: 0.0 }.meters()

    fun averageDistance(): Distance? {
        val distances = activities.mapNotNull { it.activity.distance?.meters }
        if (distances.isEmpty()) {
            return null
        }
        return (distances.sum().toDouble() / distances.size).meters()
    }

    fun averageMovingSpeed(): Speed? {
        val speeds = activities.mapNotNull { it.activity.averageMovingSpeed?.metersPerSecond }
        if (speeds.isEmpty()) {
            return null
        }
        return (speeds.sum() / speeds.size).metersPerSecond()
    }

    fun averageSpeed(): Speed? {
        val speeds = activities.mapNotNull { it.activity.averageSpeed?.metersPerSecond }
        if (speeds.isEmpty()) {
            return null
        }
        return (speeds.sum() / speeds.size).metersPerSecond()
    }

    fun averageHeartRate(): HeartFrequency? {
        val heartRates = activities.mapNotNull { it.activity.averageHeartRate?.bpm }
        if (heartRates.isEmpty()) {
            return null
        }
        return (heartRates.sum() / heartRates.size).bpm()
    }

    fun averageAscent(): VerticalDistance? {
        val ascents = activities.mapNotNull { it.activity.totalAscent?.meters }
        if (ascents.isEmpty()) {
            return null
        }
        return VerticalDistance(meters = ascents.sum() / ascents.size)
    }

    fun averageDescent(): VerticalDistance? {
        val descents = activities.mapNotNull { it.activity.totalDescent?.meters }
        if (descents.isEmpty()) {
            return null
        }
        return VerticalDistance(meters = descents.sum() / descents.size)
    }

    fun totalAscent(): VerticalDistance =
        VerticalDistance(meters = activities.sumOf { it.activity.totalAscent?.meters ?: 0.0 })

    fun totalDescent(): VerticalDistance =
        VerticalDistance(meters = activities.sumOf { it.activity.totalDescent?.meters ?: 0.0 })

    fun averageTemperature(): Temperature? {
        val temperatures = activities.mapNotNull { it.activity.temperature?.celsius }
        if (temperatures.isEmpty()) {
            return null
        }
        return (temperatures.sum() / temperatures.size).celsius()
    }

    fun averageIntensity(): Double? {
        val intensities = activities.mapNotNull { it.activity.intensity?.value }
        if (intensities.isEmpty()) {
            return null
        }
        return intensities.sum().toDouble() / intensities.size
    }

    fun maximalDuration(): RichActivity? = activities.maxByOrNull { it.activity.duration.elapsedMs }

    fun maximalDistance(): RichActivity? =
        activities.filteredMaxByOrNull { it.activity.distance?.meters }

    fun maximalAverageMovingSpeed(): RichActivity? =
        activities.filteredMaxByOrNull { it.activity.averageMovingSpeed }

    fun maximalAscent(): RichActivity? =
        activities.filteredMaxByOrNull { it.activity.totalAscent?.meters }

    fun maximalHeartRate(): RichActivity? =
        activities.filteredMaxByOrNull { it.activity.maximalHeartRate }

    fun isEmpty() = activities.isEmpty()

    val activitiesByPlace: Map<Place?, ActivityStatistics>
        get() = activities
            .groupBy { it.place }
            .toList()
            .sortedBy { (_, v) -> -v.size }
            .toMap()
            .mapValues { ActivityStatistics(it.value) }

    val activitiesByFeel: Map<Feel, ActivityStatistics>
        get() = activities
            .groupBy { it.activity.feel }
            .toList()
            .sortedBy { (_, v) -> -v.size }
            .toMap()
            .mapValues { ActivityStatistics(it.value) }

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

    val activitiesByWeekday: Map<DayOfWeek, ActivityStatistics>
        get() {
            val zoneId = ZoneId.systemDefault()
            return groupBy { it.activity.startTime.toLocalDate(zoneId).dayOfWeek }
        }

    val activitiesByHourOfDay: Map<Int, ActivityStatistics>
        get() {
            val zoneId = ZoneId.systemDefault()
            return groupBy { it.activity.startTime.toLocalDateTime(zoneId).hour }
        }

    private inline fun groupByLocalDate(func: (LocalDate) -> LocalDate): Map<LocalDate, ActivityStatistics> {
        val zoneId = ZoneId.systemDefault()
        return groupBy { func(it.activity.startTime.toLocalDate(zoneId)) }
    }

    private inline fun <T> groupBy(func: (RichActivity) -> T): Map<T, ActivityStatistics> =
        activities.groupBy(func).mapValues { ActivityStatistics(it.value) }

    inline fun filter(func: (RichActivity) -> Boolean) = ActivityStatistics(activities.filter(func))

    private fun keepNewer(date: Date): ActivityStatistics =
        filter { it.activity.startTime.after(date) }

    private fun keepNewerThanOneYear() =
        keepNewer(LocalDate.now().minusYears(1).plusDays(1).atStartOfDay().toDate())
}