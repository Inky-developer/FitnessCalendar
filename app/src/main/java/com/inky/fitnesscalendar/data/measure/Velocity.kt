package com.inky.fitnesscalendar.data.measure

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Velocity(val metersPerSecond: Double) : Parcelable {
    val kmh
        get() = metersPerSecond * 3.6

    companion object {
        fun kilometersPerHour(kmh: Double) = Velocity(kmh / 3.6)
    }
}