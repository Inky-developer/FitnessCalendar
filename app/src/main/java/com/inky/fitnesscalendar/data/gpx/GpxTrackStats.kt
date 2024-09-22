package com.inky.fitnesscalendar.data.gpx

import com.inky.fitnesscalendar.data.measure.Distance
import com.inky.fitnesscalendar.data.measure.Duration

data class GpxTrackStats(
    val totalDistance: Distance,
    val totalDuration: Duration,
    val movingDuration: Duration
)