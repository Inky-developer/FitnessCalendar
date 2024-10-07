package com.inky.fitnesscalendar.data.gpx

import com.inky.fitnesscalendar.data.measure.Distance
import com.inky.fitnesscalendar.data.measure.Duration
import com.inky.fitnesscalendar.data.measure.Elevation
import com.inky.fitnesscalendar.data.measure.HeartFrequency
import com.inky.fitnesscalendar.data.measure.Temperature

data class GpxTrackStats(
    val totalDistance: Distance,
    val totalDuration: Duration,
    val movingDuration: Duration,
    val averageHeartFrequency: HeartFrequency?,
    val maxHeartFrequency: HeartFrequency?,
    val averageTemperature: Temperature?,
    val minTemperature: Temperature?,
    val maxTemperature: Temperature?,
    val minHeight: Elevation?,
    val maxHeight: Elevation?
)