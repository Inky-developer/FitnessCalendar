package com.inky.fitnesscalendar.data.gpx

import com.inky.fitnesscalendar.data.measure.Distance
import com.inky.fitnesscalendar.data.measure.Duration.Companion.until
import kotlin.math.roundToLong

data class GpxTrack(val name: String, val type: String, val points: List<GpxTrackPoint>) {
    val startTime get() = points.firstOrNull()?.time
    val endTime get() = points.lastOrNull()?.time

    val duration = endTime?.let { startTime?.until(it) }

    fun computeLength(): Distance {
        val cachedResultsArray = FloatArray(3)
        val distanceMeters = points
            .windowed(2)
            .sumOf { it[0].coordinate.distanceMeters(it[1].coordinate, cachedResultsArray) }

        return Distance(meters = distanceMeters.roundToLong())
    }
}
