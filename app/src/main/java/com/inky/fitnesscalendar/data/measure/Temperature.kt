package com.inky.fitnesscalendar.data.measure

import android.content.Context
import com.inky.fitnesscalendar.R
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Temperature(val celsius: Float) : Comparable<Temperature> {
    override fun compareTo(other: Temperature) = this.celsius.compareTo(other.celsius)

    fun format(context: Context) =
        context.getString(R.string.x_degrees_celsius, "%.1f".format(celsius))
}