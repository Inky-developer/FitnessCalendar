package com.inky.fitnesscalendar.data.measure

import android.content.Context
import android.os.Parcelable
import com.inky.fitnesscalendar.R
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt


@Parcelize
@JvmInline
value class Distance internal constructor(val meters: Double) : Parcelable, Measure {
    override fun formatWithContext(context: Context) = if (kilometers >= 1) {
        context.getString(R.string.x_km, "%.1f".format(kilometers))
    } else {
        context.getString(R.string.x_m, meters.roundToInt())
    }

    override fun isNothing() = meters == 0.0

    val kilometers
        get() = meters / 1000.0
}

fun Double.meters() = Distance(meters = this)

fun Double.kilometers() = Distance(meters = 1000 * this)