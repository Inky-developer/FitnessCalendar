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

    private fun computeStatistics(): GpxTrackStats? {
        val duration = totalDuration ?: return null

        val trackPointComputedData = computeTrackPointComputedData()

        val totalDistance =
            Distance(meters = trackPointComputedData.sumOf { it.distanceMeters }.roundToLong())
        val movingDuration = Duration(
            elapsedMs = trackPointComputedData
                .filter { it.speed.kmh > 3 }
                .sumOf { it.duration.elapsedMs }
        )

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

        return GpxTrackStats(
            totalDistance = totalDistance,
            totalDuration = duration,
            movingDuration = movingDuration,
            averageHeartFrequency = averageHeartRate,
            maxHeartFrequency = maxHeartRate,
            averageTemperature = averageTemperature,
            minTemperature = minTemperature,
            maxTemperature = maxTemperature
        )
    }

    private fun computeTrackPointComputedData(): List<TrackPointComputedData> {
        val cachedDistanceResultsArray = FloatArray(3)
        val computedData = points
            .windowed(2)
            .map { (a, b) ->
                val distance = a.coordinate.distanceMeters(b.coordinate, cachedDistanceResultsArray)
                val duration = a.time.until(b.time)
                TrackPointComputedData(distance, duration)
            }

        return computedData
    }

    private data class TrackPointComputedData(val distanceMeters: Double, val duration: Duration) {
        val speed get() = Speed(metersPerSecond = distanceMeters / duration.elapsedSeconds)
    }
}
