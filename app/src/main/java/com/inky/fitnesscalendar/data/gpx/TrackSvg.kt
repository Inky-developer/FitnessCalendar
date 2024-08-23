package com.inky.fitnesscalendar.data.gpx

import com.inky.fitnesscalendar.db.entities.CoordinateRect
import com.inky.fitnesscalendar.db.entities.CoordinateRect.Companion.calculateBounds
import com.inky.fitnesscalendar.util.gpx.simplify
import kotlinx.serialization.Serializable


@Serializable
data class TrackSvg(val points: List<Coordinate>, val bounds: CoordinateRect) {
    companion object {
        fun fromPoints(points: List<Coordinate>): TrackSvg? {
            val bounds = points.calculateBounds() ?: return null
            return TrackSvg(points, bounds)
        }

        fun GpxTrack.toTrackSvg() = fromPoints(simplify(points.map { it.coordinate }))
    }
}