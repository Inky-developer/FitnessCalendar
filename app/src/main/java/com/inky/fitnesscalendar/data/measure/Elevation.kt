package com.inky.fitnesscalendar.data.measure

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Elevation(val meters: Float) : Comparable<Elevation> {
    override fun compareTo(other: Elevation) = this.meters.compareTo(other.meters)

}