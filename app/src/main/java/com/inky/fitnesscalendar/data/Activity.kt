package com.inky.fitnesscalendar.data

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Activity(
    @PrimaryKey val uid: Int? = null,
    @ColumnInfo(name = "type") val type: ActivityType,
    @ColumnInfo(name = "vehicle") val vehicle: Vehicle? = null,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "start_time") val startTime: Date,
    @ColumnInfo(name = "end_time") val endTime: Date = startTime,
    @ColumnInfo(name = "feel") val feel: Feel? = null,
    @ColumnInfo(name = "image_uri") val imageUri: Uri? = null
) {
    fun clean() = copy(
        vehicle = if (type.hasVehicle) vehicle else null,
        endTime = if (type.hasDuration) endTime else startTime
    )
}
