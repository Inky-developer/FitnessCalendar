package com.inky.fitnesscalendar.db.entities

import com.inky.fitnesscalendar.data.gpx.Coordinate
import kotlinx.serialization.Serializable

@Serializable
data class CoordinateRect(
    val latitudeMin: Double,
    val latitudeMax: Double,
    val longitudeMin: Double,
    val longitudeMax: Double
) {
    companion object {
        fun List<Coordinate>.calculateBounds(): CoordinateRect? {
            if (isEmpty()) {
                return null
            }

            var latitudeMin = this[0].latitude
            var latitudeMax = this[0].latitude
            var longitudeMin = this[0].longitude
            var longitudeMax = this[0].longitude

            for (point in this) {
                if (point.latitude < latitudeMin) latitudeMin = point.latitude
                if (point.latitude > latitudeMax) latitudeMax = point.latitude

                if (point.longitude < longitudeMin) longitudeMin =
                    point.longitude
                if (point.longitude > longitudeMax) longitudeMax =
                    point.longitude
            }

            return CoordinateRect(
                latitudeMin = latitudeMin,
                latitudeMax = latitudeMax,
                longitudeMin = longitudeMin,
                longitudeMax = longitudeMax
            )
        }
    }
}