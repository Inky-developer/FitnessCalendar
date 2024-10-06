package com.inky.fitnesscalendar.data.measure

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Temperature(val celsius: Float) : Comparable<Temperature> {
    override fun compareTo(other: Temperature) = this.celsius.compareTo(other.celsius)
}