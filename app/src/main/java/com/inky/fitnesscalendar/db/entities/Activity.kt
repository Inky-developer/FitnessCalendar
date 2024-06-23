package com.inky.fitnesscalendar.db.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.data.Vehicle
import java.util.Date

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ActivityType::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf("type_id"),
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class Activity(
    @PrimaryKey val uid: Int? = null,
    @ColumnInfo(name = "type_id") val typeId: Int,
    @ColumnInfo(name = "vehicle") val vehicle: Vehicle? = null,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "start_time", index = true) val startTime: Date,
    @ColumnInfo(name = "end_time") val endTime: Date = startTime,
    @ColumnInfo(name = "feel") val feel: Feel? = null,
    @ColumnInfo(name = "image_uri") val imageUri: Uri? = null
) {
    fun clean(type: ActivityType) = copy(
        vehicle = if (type.hasVehicle) vehicle else null,
        endTime = if (type.hasDuration) endTime else startTime
    )
}
