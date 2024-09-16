package com.inky.fitnesscalendar.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.data.ImageName

@Entity
data class Day(
    @PrimaryKey @ColumnInfo(name = "day") val day: EpochDay,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "feel") val feel: Feel? = null,
    @ColumnInfo(name = "image_name") val imageName: ImageName? = null
)