package com.inky.fitnesscalendar.data.measure

import android.content.Context
import android.os.Parcelable
import com.inky.fitnesscalendar.R
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class Speed internal constructor(val metersPerSecond: Double) : Parcelable, Measure,
    Comparable<Speed> {
    val kmh get() = metersPerSecond * 3.6

    override fun formatWithContext(context: Context) =
        context.getString(R.string.x_kmh, "%.1f".format(kmh))

    override fun compareTo(other: Speed) = this.metersPerSecond.compareTo(other.metersPerSecond)

    override fun isNothing() = metersPerSecond == 0.0

    operator fun minus(other: Speed) =
        (this.metersPerSecond - other.metersPerSecond).metersPerSecond()
}

fun Double.metersPerSecond() = Speed(metersPerSecond = this)
fun Int.metersPerSecond() = toDouble().metersPerSecond()

fun Double.kmh() = Speed(metersPerSecond = this / 3.6)
fun Int.kmh() = toDouble().kmh()