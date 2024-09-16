package com.inky.fitnesscalendar.data

import android.content.Context
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.util.getOrCreateImagesDir
import kotlinx.parcelize.Parcelize
import java.nio.file.Path

/**
 * An image name is the name of an image for e.g. [Activity] or [Day].
 * Each image name has an associated file path to the actual image
 */
@Parcelize
@JvmInline
value class ImageName(val name: String) : Parcelable {
    @Composable
    fun getImageUri() = getImageUri(LocalContext.current)

    fun getImageUri(context: Context) = resolve(context.getOrCreateImagesDir().toPath())

    fun resolve(imagesDir: Path) = imagesDir.resolve(name).toFile().toUri()
}