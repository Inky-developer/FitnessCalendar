package com.inky.fitnesscalendar.db.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.inky.fitnesscalendar.data.ImageName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Activity::class,
            parentColumns = ["uid"],
            childColumns = ["activity_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("activity_id")]
)
data class ActivityImage(
    @ColumnInfo(name = "activity_id") val activityId: Int?,
    @PrimaryKey @ColumnInfo(name = "image_name") val imageName: ImageName
) : Parcelable