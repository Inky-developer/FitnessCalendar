package com.inky.fitnesscalendar.data

import android.content.Context
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
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
) : Displayable, Parcelable {
    fun hasFeel() = hasDuration

    override fun getColor(context: Context) = context.getColor(color.colorId)

    override fun getText(context: Context) = name
}