package com.inky.fitnesscalendar.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.data.ImageName
import com.inky.fitnesscalendar.data.Intensity
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.data.gpx.TrackSvg
import com.inky.fitnesscalendar.data.measure.Distance
import com.inky.fitnesscalendar.data.measure.Duration.Companion.until
import com.inky.fitnesscalendar.data.measure.Velocity
import com.inky.fitnesscalendar.util.toLocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    @ColumnInfo(name = "favorite", defaultValue = "false") val favorite: Boolean = false,

    @ColumnInfo(name = "image_name") val imageName: ImageName? = null,
    @ColumnInfo(name = "feel") val feel: Feel? = null,
    @ColumnInfo(name = "intensity") val intensity: Intensity? = null,

    @ColumnInfo(name = "start_time", index = true) val startTime: Date,
    @ColumnInfo(name = "end_time") val endTime: Date = startTime,
    @ColumnInfo(name = "distance") val distance: Distance? = null,

    // The unique identifier of the wifi network the device was connected to when starting the activity
    @ColumnInfo(name = "wifi_bssid") val wifiBssid: String? = null,
    @ColumnInfo(name = "track_preview") val trackPreview: SerializedTrackPreview? = null,
) {
    fun clean(type: ActivityType) = copy(
        placeId = if (type.hasPlace) placeId else null,
        vehicle = if (type.hasVehicle) vehicle else null,
        endTime = maxOf(if (type.hasDuration) endTime else startTime, startTime),
        distance = if (type.hasDistance) distance else null,
        intensity = if (type.hasIntensity) intensity else null,
    )

    val epochDay get() = EpochDay(startTime.toLocalDate().toEpochDay())

    val duration
        get() = startTime until endTime

    val velocity: Velocity?
        get() {
            if (duration.elapsedMs == 0L) {
                return null
            }
            return distance?.let { Velocity(metersPerSecond = it.meters / duration.elapsedSeconds) }
        }

    @JvmInline
    value class SerializedTrackPreview(private val inner: String) {
        val value get() = inner

        fun toTrackSvg(): TrackSvg = Json.decodeFromString(value)

        companion object {
            fun assumeValid(value: String) = SerializedTrackPreview(value)

            fun TrackSvg.serialize() = SerializedTrackPreview(Json.encodeToString(this))
        }
    }
}
