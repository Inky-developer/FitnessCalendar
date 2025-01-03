package com.inky.fitnesscalendar.data.measure

import android.content.Context
import android.os.Parcelable
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.ui.util.ContextFormat
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToLong


@Parcelize
@JvmInline
value class Distance internal constructor(val meters: Long) : Parcelable, ContextFormat {
    override fun formatWithContext(context: Context) = if (kilometers >= 1) {
        context.getString(R.string.x_km, "%.1f".format(kilometers))
    } else {
        context.getString(R.string.x_m, meters)
    }

    val kilometers
        get() = meters / 1000.0
}

fun Long.meters() = Distance(meters = this)
fun Double.meters() = roundToLong().meters()

fun Long.kilometers() = Distance(meters = this * 1000)
fun Int.kilometers() = toLong().kilometers()
fun Double.kilometers() = roundToLong().kilometers()