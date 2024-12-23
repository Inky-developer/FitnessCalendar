package com.inky.fitnesscalendar.data.measure

import android.content.Context
import android.os.Parcelable
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.ui.util.ContextFormat
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class Speed(val metersPerSecond: Double) : Parcelable, ContextFormat {
    val kmh get() = metersPerSecond * 3.6

    override fun formatWithContext(context: Context) = context.getString(R.string.x_kmh, "%.1f".format(kmh))

    operator fun minus(other: Speed) =
        Speed(metersPerSecond = this.metersPerSecond - other.metersPerSecond)
}