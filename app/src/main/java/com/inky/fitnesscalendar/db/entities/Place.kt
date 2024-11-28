package com.inky.fitnesscalendar.db.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.inky.fitnesscalendar.data.ContentColor
import com.inky.fitnesscalendar.data.ImageName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Place(
    @PrimaryKey val uid: Int? = null,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "color") val color: ContentColor,
    @ColumnInfo(name = "image_name") val imageName: ImageName? = null
) : Parcelable {
    override fun toString() = name
}