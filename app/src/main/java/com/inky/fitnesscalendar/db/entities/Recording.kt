package com.inky.fitnesscalendar.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.inky.fitnesscalendar.data.Vehicle
import java.time.Instant
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
data class Recording(
    @PrimaryKey val uid: Int? = null,
    @ColumnInfo(name = "type_id") val typeId: Int,
    @ColumnInfo(name = "start_time") val startTime: Date,
    @ColumnInfo(name = "vehicle") val vehicle: Vehicle? = null,
    @ColumnInfo(name = "place_id") val placeId: Int? = null,
) {
    fun toActivity(type: ActivityType, endTime: Date = Date.from(Instant.now())) = Activity(
        typeId = typeId,
        startTime = startTime,
        endTime = endTime,
        vehicle = vehicle,
        placeId = placeId,
    ).clean(type)
}