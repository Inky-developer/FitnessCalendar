package com.inky.fitnesscalendar.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.inky.fitnesscalendar.data.gpx.GpxTrackPoint
import com.inky.fitnesscalendar.data.gpx.GpxTrackStats
import com.inky.fitnesscalendar.data.measure.Distance
import com.inky.fitnesscalendar.data.measure.Duration
import com.inky.fitnesscalendar.data.measure.Duration.Companion.until
import com.inky.fitnesscalendar.data.measure.HeartFrequency
import com.inky.fitnesscalendar.data.measure.Speed
import com.inky.fitnesscalendar.data.measure.Temperature
import kotlin.math.roundToLong

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Activity::class,
            parentColumns = ["uid"],
            childColumns = ["activity_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("activity_id")]
)
data class Track(
    @PrimaryKey val uid: Int? = null,
    @ColumnInfo(name = "activity_id") val activityId: Int,
    @ColumnInfo(name = "points") val points: List<GpxTrackPoint>
) {
    val startTime get() = points.firstOrNull()?.time
    val endTime get() = points.lastOrNull()?.time

    private val totalDuration get() = endTime?.let { startTime?.until(it) }

    /**
     * Adds all relevant data of this track to the activity
     */
    fun addStatsToActivity(activity: Activity): Activity? {
        val stats = computeStatistics() ?: return null
        return activity.copy(
            startTime = startTime ?: return null,
            endTime = endTime ?: return null,
            distance = stats.totalDistance,
            movingDuration = stats.movingDuration,
            temperature = stats.averageTemperature,
            averageHeartRate = stats.averageHeartFrequency,
            maximalHeartRate = stats.maxHeartFrequency
        )
    }

    fun computedPoints(): List<ComputedTrackPoint> {
        if (points.isEmpty()) {
            return emptyList()
        }

        val cachedDistanceResultsArray = FloatArray(3)
        var cumDistanceMeters = 0.0
        val firstComputedTrackPoint = ComputedTrackPoint(
            distanceMeters = 0.0,
            cumDistance = Distance(meters = cumDistanceMeters.roundToLong()),
            duration = Duration(elapsedMs = 0L),
            point = points[0]
        )

        val computedPoints = mutableListOf(firstComputedTrackPoint)
        points
            .windowed(2)
            .mapTo(computedPoints) { (a, b) ->
                val distance = a.coordinate.distanceMeters(b.coordinate, cachedDistanceResultsArray)
                cumDistanceMeters += distance
                val duration = a.time.until(b.time)
                ComputedTrackPoint(
                    distanceMeters = distance,
                    cumDistance = Distance(meters = cumDistanceMeters.roundToLong()),
                    duration = duration,
                    point = b,
                )
            }

        return computedPoints
    }

    fun computeStatistics(): GpxTrackStats? {
        val duration = totalDuration ?: return null

        val trackPointComputedData = computedPoints()

        val totalDistance =
            Distance(meters = trackPointComputedData.sumOf { it.distanceMeters }.roundToLong())
        val movingDuration = Duration(
            elapsedMs = trackPointComputedData
                .filter { it.speed.kmh > 3 }
                .sumOf { it.duration.elapsedMs }
        )

        val maxSpeed = trackPointComputedData.maxByOrNull { it.speed.metersPerSecond }?.speed
            ?: Speed(metersPerSecond = 0.0)

        val averageHeartRate = points.mapNotNull { it.heartFrequency?.bpm }.let {
            if (it.isEmpty()) {
                null
            } else {
                HeartFrequency(bpm = it.sum() / it.size)
            }
        }
        val maxHeartRate = points.mapNotNull { it.heartFrequency }.maxByOrNull { it }

        val averageTemperature = points
            .mapNotNull { it.temperature?.celsius }
            .let {
                if (it.isEmpty()) {
                    null
                } else {
                    Temperature(celsius = it.sum() / it.size)
                }
            }
        val minTemperature = points.mapNotNull { it.temperature }.minByOrNull { it }
        val maxTemperature = points.mapNotNull { it.temperature }.maxByOrNull { it }

        val minHeight = points.mapNotNull { it.elevation }.minByOrNull { it }
        val maxHeight = points.mapNotNull { it.elevation }.maxByOrNull { it }

        return GpxTrackStats(
            totalDistance = totalDistance,
            totalDuration = duration,
            movingDuration = movingDuration,
            maxSpeed = maxSpeed,
            averageHeartFrequency = averageHeartRate,
            maxHeartFrequency = maxHeartRate,
            averageTemperature = averageTemperature,
            minTemperature = minTemperature,
            maxTemperature = maxTemperature,
            minHeight = minHeight,
            maxHeight = maxHeight
        )
    }

    data class ComputedTrackPoint(
        val distanceMeters: Double,
        val cumDistance: Distance,
        val duration: Duration,
        val point: GpxTrackPoint
    ) {
        val speed
            get() = if (duration.elapsedMs > 0)
                Speed(metersPerSecond = distanceMeters / duration.elapsedSeconds)
            else
                Speed(metersPerSecond = 0.0)
    }
}
