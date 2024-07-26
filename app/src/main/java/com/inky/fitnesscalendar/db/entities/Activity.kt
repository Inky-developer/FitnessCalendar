package com.inky.fitnesscalendar.db.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.data.Intensity
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.data.measure.Distance
import com.inky.fitnesscalendar.data.measure.Duration.Companion.until
import com.inky.fitnesscalendar.data.measure.Velocity
import java.util.Date

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ActivityType::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf("type_id"),
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Place::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf("place_id"),
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("type_id"), Index("place_id")]
)
data class Activity(
    @PrimaryKey val uid: Int? = null,
    @ColumnInfo(name = "type_id") val typeId: Int,
    @ColumnInfo(name = "place_id") val placeId: Int? = null,
    @ColumnInfo(name = "vehicle") val vehicle: Vehicle? = null,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "start_time", index = true) val startTime: Date,
    @ColumnInfo(name = "end_time") val endTime: Date = startTime,
    @ColumnInfo(name = "feel") val feel: Feel? = null,
    @ColumnInfo(name = "image_uri") val imageUri: Uri? = null,
    @ColumnInfo(name = "distance") val distance: Distance? = null,
    @ColumnInfo(name = "intensity") val intensity: Intensity? = null,
    // The unique identifier of the wifi network the device was connected to when starting the activity
    @ColumnInfo(name = "wifi_bssid") val wifiBssid: String? = null,
) {
    fun clean(type: ActivityType) = copy(
        placeId = if (type.hasPlace) placeId else null,
        vehicle = if (type.hasVehicle) vehicle else null,
        endTime = maxOf(if (type.hasDuration) endTime else startTime, startTime),
        distance = if (type.hasDistance) distance else null,
        intensity = if (type.hasIntensity) intensity else null,
    )

    val duration
        get() = startTime until endTime

    val velocity: Velocity?
        get() {
            if (duration.elapsedMs == 0L) {
                return null
            }
            return distance?.let { Velocity(metersPerSecond = it.meters / duration.elapsedSeconds) }
        }
}
