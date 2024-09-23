package com.inky.fitnesscalendar.data.gpx

data class GpxTrack(val name: String, val type: String, val points: List<GpxTrackPoint>) {
    val startTime = points.firstOrNull()?.time
    val endTime = points.lastOrNull()?.time
}
