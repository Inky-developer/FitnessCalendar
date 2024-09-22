package com.inky.fitnesscalendar.data.measure

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class Speed(val metersPerSecond: Double) : Parcelable {
    val kmh get() = metersPerSecond * 3.6
}