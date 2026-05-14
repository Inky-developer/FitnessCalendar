package com.inky.fitnesscalendar.db.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.inky.fitnesscalendar.data.ImageName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserImage(
    @ColumnInfo(name = "image_name") val name: ImageName,
    @ColumnInfo(name = "horizontal_bias", defaultValue = "0") val horizontalBias: Float = 0f,
    @ColumnInfo(name = "vertical_bias", defaultValue = "0") val verticalBias: Float = 0f,
) : Parcelable
