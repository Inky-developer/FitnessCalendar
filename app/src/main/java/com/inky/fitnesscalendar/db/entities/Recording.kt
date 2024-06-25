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
        )
    ],
    indices = [Index("type_id")]
)
data class Recording(
    @PrimaryKey val uid: Int? = null,
    @ColumnInfo(name = "type_id") val typeId: Int,
    @ColumnInfo(name = "vehicle") val vehicle: Vehicle? = null,
    @ColumnInfo(name = "start_time") val startTime: Date,
) {
    fun toActivity(endTime: Date = Date.from(Instant.now())) = Activity(
        typeId = typeId,
        vehicle = vehicle,
        startTime = startTime,
        endTime = endTime
    )
}