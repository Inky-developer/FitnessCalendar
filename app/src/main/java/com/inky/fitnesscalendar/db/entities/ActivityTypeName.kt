package com.inky.fitnesscalendar.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Maps a string to an activity type.
 * For example, when importing gpx files, this will be used to convert the activity type
 * specified in the gpx file to an [ActivityType]
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ActivityType::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf("type_id"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("type_id")]
)
data class ActivityTypeName(
    @PrimaryKey val name: String,
    @ColumnInfo(name = "type_id") val typeId: Int
)