package com.inky.fitnesscalendar.data.measure

import android.content.Context
import com.inky.fitnesscalendar.R
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
@JvmInline
value class Elevation(val meters: Double) : Comparable<Elevation>, Measure {
    override fun compareTo(other: Elevation) = this.meters.compareTo(other.meters)

    override fun isNothing() = false

    override fun formatWithContext(context: Context) =
        context.getString(R.string.x_m, meters.roundToInt())
}