package com.inky.fitnesscalendar.data.measure

import android.content.Context
import android.os.Parcelable
import com.inky.fitnesscalendar.R
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class Speed(val metersPerSecond: Double) : Parcelable {
    val kmh get() = metersPerSecond * 3.6

    fun format(context: Context) = context.getString(R.string.x_kmh, "%.1f".format(kmh))

    operator fun minus(other: Speed) =
        Speed(metersPerSecond = this.metersPerSecond - other.metersPerSecond)
}