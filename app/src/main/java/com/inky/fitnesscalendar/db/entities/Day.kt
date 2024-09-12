package com.inky.fitnesscalendar.db.entities

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.util.getOrCreateImagesDir

@Entity
data class Day(
    @PrimaryKey @ColumnInfo(name = "day") val day: EpochDay,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "feel") val feel: Feel? = null,
    @ColumnInfo(name = "image_name") val imageName: String? = null
) {
    @Composable
    fun getImageUri(context: Context = LocalContext.current) =
        imageName?.let { context.getOrCreateImagesDir().toPath().resolve(it).toFile().toUri() }
}