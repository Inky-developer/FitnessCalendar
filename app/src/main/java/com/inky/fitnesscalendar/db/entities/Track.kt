package com.inky.fitnesscalendar.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.inky.fitnesscalendar.data.gpx.GpxTrackPoint

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Activity::class,
            parentColumns = ["uid"],
            childColumns = ["activity_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("activity_id")]
)
data class Track(
    @PrimaryKey val uid: Int? = null,
    @ColumnInfo(name = "activity_id") val activityId: Int,
    @ColumnInfo(name = "points") val points: List<GpxTrackPoint>
)
