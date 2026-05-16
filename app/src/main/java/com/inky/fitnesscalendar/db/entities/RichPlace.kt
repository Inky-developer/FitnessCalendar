package com.inky.fitnesscalendar.db.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class RichPlace(
    @Embedded
    val place: Place,
    @ColumnInfo(name = "count")
    val numActivities: Int
)