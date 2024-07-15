package com.inky.fitnesscalendar.db.entities

import android.content.Context
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ActivityTypeColor
import com.inky.fitnesscalendar.data.Displayable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class ActivityType(
    @PrimaryKey val uid: Int? = null,
    @ColumnInfo(name = "activity_category") val activityCategory: ActivityCategory,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "emoji") val emoji: String,
    @ColumnInfo(name = "color") val color: ActivityTypeColor,
    @ColumnInfo(name = "has_vehicle") val hasVehicle: Boolean = false,
    @ColumnInfo(name = "has_duration") val hasDuration: Boolean = true,
    @ColumnInfo(name = "has_distance", defaultValue = "false") val hasDistance: Boolean = false,
    @ColumnInfo(name = "has_intensity", defaultValue = "false") val hasIntensity: Boolean = false,
) : Displayable, Parcelable {
    fun hasFeel() = hasDuration

    override fun getColor(context: Context) = context.getColor(color.colorId)

    override fun getText(context: Context) = name

    override fun getShortText() = emoji
}