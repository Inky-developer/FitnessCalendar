package com.inky.fitnesscalendar.db.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.Feel

@Entity
data class Day(
    @PrimaryKey @ColumnInfo(name = "day") val day: EpochDay,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "feel") val feel: Feel? = null,
    @ColumnInfo(name = "image_uri") val imageUri: Uri? = null
)