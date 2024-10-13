package com.inky.fitnesscalendar.data.gpx

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class GpxTrack(val name: String, val type: String, val points: List<GpxTrackPoint>) {
    @Transient
    val startTime = points.firstOrNull()?.time

    @Transient
    val endTime = points.lastOrNull()?.time
}
