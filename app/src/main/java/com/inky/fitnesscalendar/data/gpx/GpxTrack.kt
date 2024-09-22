package com.inky.fitnesscalendar.data.gpx

import com.inky.fitnesscalendar.data.measure.Distance
import com.inky.fitnesscalendar.data.measure.Duration
import com.inky.fitnesscalendar.data.measure.Duration.Companion.until
import com.inky.fitnesscalendar.data.measure.Speed
import kotlin.math.roundToLong

data class GpxTrack(val name: String, val type: String, val points: List<GpxTrackPoint>) {
    val startTime = points.firstOrNull()?.time
    val endTime = points.lastOrNull()?.time

    private val totalDuration = endTime?.let { startTime?.until(it) }

    fun computeStatistics(): GpxTrackStats? {
        val duration = totalDuration ?: return null

        val trackPointComputedData = computeTrackPointComputedData()

        val totalDistance =
            Distance(meters = trackPointComputedData.sumOf { it.distanceMeters }.roundToLong())
        val movingDuration = Duration(
            elapsedMs = trackPointComputedData
                .filter { it.speed.kmh > 3 }
                .sumOf { it.duration.elapsedMs }
        )

        return GpxTrackStats(
            totalDistance = totalDistance,
            totalDuration = duration,
            movingDuration = movingDuration
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
