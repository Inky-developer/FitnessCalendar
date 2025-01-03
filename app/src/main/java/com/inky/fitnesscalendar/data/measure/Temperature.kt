package com.inky.fitnesscalendar.data.measure

import android.content.Context
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.ui.util.ContextFormat
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Temperature(val celsius: Float) : Comparable<Temperature>, ContextFormat {
    override fun compareTo(other: Temperature) = this.celsius.compareTo(other.celsius)

    override fun formatWithContext(context: Context) =
        context.getString(R.string.x_degrees_celsius, "%.1f".format(celsius))
}

fun Float.celsius() = Temperature(celsius = this)