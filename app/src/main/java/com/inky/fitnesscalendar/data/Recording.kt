package com.inky.fitnesscalendar.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.Date

@Entity
data class Recording(
    @PrimaryKey val uid: Int? = null,
    @ColumnInfo(name = "type") val type: ActivityType,
    @ColumnInfo(name = "vehicle") val vehicle: Vehicle? = null,
    @ColumnInfo(name = "start_time") val startTime: Date,
) {
    fun toActivity(endTime: Date = Date.from(Instant.now())) = Activity(
        type = type,
        vehicle = vehicle,
        startTime = startTime,
        endTime = endTime
    )
}