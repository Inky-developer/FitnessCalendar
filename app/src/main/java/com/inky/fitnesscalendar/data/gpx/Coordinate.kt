package com.inky.fitnesscalendar.data.gpx

import android.location.Location

data class Coordinate(val latitude: Double, val longitude: Double) {
    fun distanceMeters(other: Coordinate, results: FloatArray): Double {
        Location.distanceBetween(
            this.latitude,
            this.longitude,
            other.latitude,
            other.longitude,
            results
        )
        return results[0].toDouble()
    }
}
