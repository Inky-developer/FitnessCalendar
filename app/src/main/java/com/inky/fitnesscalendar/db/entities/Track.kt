package com.inky.fitnesscalendar.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.inky.fitnesscalendar.data.gpx.GpxTrackPoint

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
    fun calculateBounds(): CoordinateRect? {
        if (points.isEmpty()) {
            return null
        }

        var latitudeMin = points[0].coordinate.latitude
        var latitudeMax = points[0].coordinate.latitude
        var longitudeMin = points[0].coordinate.longitude
        var longitudeMax = points[0].coordinate.longitude

        for (point in points) {
            if (point.coordinate.latitude < latitudeMin) latitudeMin = point.coordinate.latitude
            if (point.coordinate.latitude > latitudeMax) latitudeMax = point.coordinate.latitude

            if (point.coordinate.longitude < longitudeMin) longitudeMin = point.coordinate.longitude
            if (point.coordinate.longitude > longitudeMax) longitudeMax = point.coordinate.longitude
        }

        return CoordinateRect(
            latitudeMin = latitudeMin,
            latitudeMax = latitudeMax,
            longitudeMin = longitudeMin,
            longitudeMax = longitudeMax
        )
    }
}
