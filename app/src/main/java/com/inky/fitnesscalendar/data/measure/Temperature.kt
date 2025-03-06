package com.inky.fitnesscalendar.data.measure

import android.content.Context
import com.inky.fitnesscalendar.R
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Temperature(val celsius: Double) : Comparable<Temperature>, Measure {
    override fun compareTo(other: Temperature) = this.celsius.compareTo(other.celsius)

    override fun formatWithContext(context: Context) =
        context.getString(R.string.x_degrees_celsius, "%.1f".format(celsius))

    override fun isNothing() = false
}

fun Double.celsius() = Temperature(celsius = this)